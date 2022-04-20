package com.bineeta.grpc.client.logic;

import com.bineeta.grpc.client.logic.utils.MulitplicationUtils;
import com.bineeta.grpc.client.storage.StorageException;
import com.bineeta.grpc.client.storage.StorageProperties;
import com.bineeta.grpc.server.MatrixReply;
import com.bineeta.grpc.server.MatrixRequest;
import com.bineeta.grpc.server.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service //comment to perform single channel grpcCalls
public class MultiChannelMultiplicationService implements MultiplicationService {
    private final Path rootLocation;
    private int[] addresses = {9090, 9092, 9093, 9090, 9092, 9093, 9090, 9092, 9093};

    @Autowired
    public MultiChannelMultiplicationService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public String getResults(MultipartFile file, double deadline, Boolean synchronous) {

        Path destinationFile = getFile(file);

        List<String> resultMatrices = new ArrayList<>();
        List<MatrixReply> blockList = null;
        MatrixReply A = null;
        ManagedChannel channel = null;
        StringBuffer str = new StringBuffer();

        List<MatrixServiceGrpc.MatrixServiceBlockingStub> availableBlockingStubs = new ArrayList<>();
        List<MatrixServiceGrpc.MatrixServiceStub> availableStubs = new ArrayList<>();
        MatrixServiceGrpc.MatrixServiceStub stub = null;

        List<List<Integer>> splitMatrices = getSplitMatrices(destinationFile);
        int totalBlock = splitMatrices.size();
        int numBlockCalls = (int) (totalBlock * Math.sqrt(totalBlock));
        int totalServer = 3;

        System.out.println("ACTUAL FUN BEGINS:" + splitMatrices);
        //MatrixReplyStreamObserver replyStream = new MatrixReplyStreamObserver(numBlock, destinationFile);

        // No of servers needed
        int numServerNeeded = getServerNeeded(splitMatrices, deadline);

        if (synchronous) {
            long startTime = System.nanoTime();
            availableBlockingStubs = new MulitplicationUtils().getAvailableChannelBlockingStubs();
            int cycle = (int) Math.sqrt(totalBlock);
            int t = 0;
            for (int w = 0; w < cycle; w++) {
                int b = 0;
                for (int i = 0; i < cycle; i++) {
                    int a = w * cycle;
                    blockList = new ArrayList<>();
                    MatrixReply added = MatrixReply.newBuilder()
                            .setC00(0).setC01(0).setC10(0).setC11(0).build();
                    str = new StringBuffer("");
                    for (int j = b; j < totalBlock; j = j + cycle) {
                        //multiplication call
                        List<Integer> M1 = splitMatrices.get(a);
                        List<Integer> M2 = splitMatrices.get(j);
                        // multiplty each block from M1 and M2
                        t++;
                        int nn = t % 3;

                        A = availableBlockingStubs.get(nn).multiplyBlock(MatrixRequest.newBuilder()//First Result Block Calculation
                                .setA00(M1.get(0)).setA01(M1.get(1)).setA10(M1.get(2)).setA11(M1.get(3))
                                .setB00(M2.get(0)).setB01(M2.get(1)).setB10(M2.get(2)).setB11(M2.get(3))
                                .build());
                        // keep on adding for each row and column
                        added = availableBlockingStubs.get(nn).addBlock(MatrixRequest.newBuilder()//First Result Block Calculation
                                .setA00(added.getC00()).setA01(added.getC01()).setA10(added.getC10()).setA11(added.getC11())
                                .setB00(A.getC00()).setB01(A.getC01()).setB10(A.getC10()).setB11(A.getC11()).build());
                        a++;
                    }
                    resultMatrices.add(str.append(added.getC00()).append(" ").append(added.getC01())
                            .append(" ").append(added.getC10()).append(" ").append(added.getC11()).toString());
                    b++;
                }
            }
            long endTime = System.nanoTime();
            long footprint = endTime - startTime;
            System.out.println("SYNCH GRPC CALL FOOTPRINT TIME: " + (footprint / 1000));

            formResult(destinationFile, resultMatrices);
        } else {
            long startTime = System.nanoTime();
            //availableStubs = new MulitplicationUtils().getAvailableChannelStubs();

            MatrixReplyStreamObserver replyStream =
                    new MatrixReplyStreamObserver(numBlockCalls, destinationFile, Boolean.TRUE);
            int t = 0;
            int cycle = (int) Math.sqrt(totalBlock);
            for (int w = 0; w < cycle; w++) {
                int b = 0;
                for (int i = 0; i < cycle; i++) {
                    int a = w * cycle;
                    blockList = new ArrayList<>();
                    MatrixReply added = MatrixReply.newBuilder()
                            .setC00(0).setC01(0).setC10(0).setC11(0).build();
                    str = new StringBuffer("");

                    int m = (w * cycle) + i;
                    for (int j = b; j < totalBlock; j = j + cycle) {
                        //multiplication call
                        List<Integer> M1 = splitMatrices.get(a);
                        List<Integer> M2 = splitMatrices.get(j);
                        // multiplty each block from M1 and M2
                        t++;
                        int nn = t % numServerNeeded;
                        System.out.println("-----------nn"+nn);
                        channel = ManagedChannelBuilder.forAddress("localhost", addresses[nn])
                                .usePlaintext().build();
                        stub = MatrixServiceGrpc.newStub(channel);
                        stub.multiplyBlock(MatrixRequest.newBuilder().setId(m + 1)
                                .setA00(M1.get(0)).setA01(M1.get(1)).setA10(M1.get(2)).setA11(M1.get(3))
                                .setB00(M2.get(0)).setB01(M2.get(1)).setB10(M2.get(2)).setB11(M2.get(3))
                                .build(), replyStream);
                        try {
                            channel.awaitTermination(2, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            System.out.println("AWAITTING CLOSURE: " + e.getLocalizedMessage());
                        }
                        a++;
                    }
                    b++;
                }
            }
        }
        return "DONE";
    }


    private int getServerNeeded(List<List<Integer>> splitMatrices, double deadline) {

        int totalBlock = splitMatrices.size();
        int numBlockCalls = (int) (totalBlock * Math.sqrt(totalBlock));

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", addresses[2])
                .usePlaintext().build();
        MatrixServiceGrpc.MatrixServiceStub stub = MatrixServiceGrpc.newStub(channel);
        MatrixReplyStreamObserver replyStream =
                new MatrixReplyStreamObserver(splitMatrices.size(),
                        new File("").toPath(), Boolean.FALSE);

        // pick random values
        List<Integer> M1 = splitMatrices.get(splitMatrices.size() - 1);
        List<Integer> M2 = splitMatrices.get(splitMatrices.size() - 1);
        long startTime = System.nanoTime();
        deadline = deadline * 1000000000; //second

        stub.multiplyBlock(MatrixRequest.newBuilder().setId(4)
                .setA00(M1.get(0)).setA01(M1.get(1)).setA10(M1.get(2)).setA11(M1.get(3))
                .setB00(M2.get(0)).setB01(M2.get(1)).setB10(M2.get(2)).setB11(M2.get(3))
                .build(), replyStream);
        long endTime = System.nanoTime();
        double footprint = endTime - startTime;
        System.out.println("startTime : " + startTime);
        System.out.println("endTime : " + endTime);
        System.out.println("Deadline : " + deadline);
        System.out.println("Footprint : " + footprint);
        System.out.println("No. Block : " + numBlockCalls);
        System.out.println("footprint*numBlockCalls : " + (footprint * numBlockCalls));

        int numberServer = (int) ((footprint * numBlockCalls) / (deadline));
        System.out.println("No. Server Needed: " + numberServer);
        channel.shutdown();

        if (numberServer > 0 && numberServer < addresses.length) {
            return numberServer;
        } else {
            return 1;
        }

    }

    @Override
    public Boolean isValid(MultipartFile file) {
        Path destinationFile = getFile(file);

        try {
            long lines = 0;
            try (Stream<String> s = Files.lines(destinationFile)) {
                lines = s.count();
            }
            if (isPowerOf2(lines)) {
                long finalLines = lines;
                Predicate<String> isInValid = i -> !isPowerOf2(i.split(" ").length) && i.split(" ").length != finalLines;
                Optional opt = null;
                try (Stream<String> s = Files.lines(destinationFile)) {
                    opt = s.filter(isInValid).findAny();
                }
                if (!opt.isPresent()) {

                    return true;
                } else {
                    System.out.println("Oooops Error");
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Path getFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }
        Path destinationFile = this.rootLocation.resolve(
                        Paths.get(file.getOriginalFilename()))
                .normalize().toAbsolutePath();
        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            // This is a security check
            throw new StorageException(
                    "Cannot store file outside current directory.");
        }
        return destinationFile;

    }

    // Utility methods to read Matrix and split the data and return list of smaller matrix

    private String formResult(Path destinationFile, List<String> resultMatrices) {
        FileWriter myWriter = null;
        StringBuffer str = new StringBuffer();
        String outputFile = destinationFile.toString().replace(".txt", "-output.txt");
        try {
            long lines = 0;
            try (Stream<String> s = Files.lines(destinationFile)) {
                lines = s.count();
            }
            int cycle = (int) lines / 2; //4=2,8=3,16=8
            myWriter = new FileWriter(outputFile);
            for (int i = 0; i < resultMatrices.size(); i = i + cycle) {
                StringBuffer tStr = new StringBuffer();
                StringBuffer bStr = new StringBuffer();
                for (int t = 0; t < cycle; t++) {
                    String[] C = resultMatrices.get(t + i).split(" ");
                    tStr.append(C[0]).append(" ").append(C[1]).append(" ");
                    bStr.append(C[2]).append(" ").append(C[3]).append(" ");
                }
                myWriter.write(tStr.append("\n").toString());
                myWriter.write(bStr.append("\n").toString());
            }
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (outputFile);

    }


    private static List<List<Integer>> getSplitMatrices(Path destinationFile) {
        List<List<Integer>> splitMatrices = new ArrayList<>();
        //File f = new File(filename);
        long lines = 0;
        //int[][] input = null;
        try (Stream<String> s = Files.lines(destinationFile)) {
            lines = s.count();
            List<List<Integer>> matrix = new ArrayList<>();
            try (Stream<String> s1 = Files.lines(destinationFile)) {
                List<String[]> data = s1.map(l -> l.split(" "))
                        .collect(Collectors.toList());
                matrix = data.stream().map(l -> method(l)).collect(Collectors.toList());
            }

            for (int i = 0; i < lines; i = i + 2) {
                for (int j = 0; j < lines; j = j + 2) {

                    List<Integer> smallMatrix = new ArrayList<>();
                    int a = i, b = j;
                    smallMatrix.add(matrix.get(a).get(b));
                    smallMatrix.add(matrix.get(a).get(b + 1));
                    smallMatrix.add(matrix.get(a + 1).get(b));
                    smallMatrix.add(matrix.get(a + 1).get(b + 1));
                    splitMatrices.add(smallMatrix);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return splitMatrices;
    }

    private static List<Integer> method(String[] l) {
        List<Integer> x = Arrays.stream(l).map(num -> Integer.parseInt(num)).collect(Collectors.toList());
        return x;

    }

    private boolean isPowerOf2(long number) {
        return (int) (Math.ceil((Math.log(number) / Math.log(2))))
                == (int) (Math.floor(((Math.log(number) / Math.log(2)))));
    }

}

