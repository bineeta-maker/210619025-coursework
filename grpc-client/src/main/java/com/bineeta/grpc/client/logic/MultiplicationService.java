package com.bineeta.grpc.client.logic;

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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MultiplicationService implements LogicService {
    private final Path rootLocation;

    @Autowired
    public MultiplicationService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public String getResults(MultipartFile file) {

        Path destinationFile = getFile(file);
        List<List<Integer>> splitMatrices = getSplitMatrices(destinationFile);
        List<String> resultMatrices = new ArrayList<>();
        System.out.println("Actual FUN begins"+splitMatrices);
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        MatrixServiceGrpc.MatrixServiceBlockingStub stub
                = MatrixServiceGrpc.newBlockingStub(channel);
        splitMatrices.forEach(smallerMatrix -> {
            //List<Integer> smallerMatrix = splitMatrices.get(0);
            StringBuffer str = new StringBuffer("");
            MatrixReply A = stub.multiplyBlock(MatrixRequest.newBuilder()//First Result Block Calculation
                    .setA00(smallerMatrix.get(0))
                    .setA01(smallerMatrix.get(1))
                    .setA10(smallerMatrix.get(2))
                    .setA11(smallerMatrix.get(3))
                    .setB00(smallerMatrix.get(0))
                    .setB01(smallerMatrix.get(1))
                    .setB10(smallerMatrix.get(2))
                    .setB11(smallerMatrix.get(3))
                    .build());

            resultMatrices.add(str.append(A.getC00()).append(" ").append(A.getC01())
                    .append(" ").append(A.getC10()).append(" ").append(A.getC11()).toString());
        });

        String str = formResult(destinationFile,resultMatrices);
        //reWriteResult();
        return str;
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
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Path getFile(MultipartFile file){

        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }
        Path destinationFile = this.rootLocation.resolve(
                        Paths.get(file.getOriginalFilename()))
                .normalize().toAbsolutePath();
        System.out.println("destinationFile :-- " + destinationFile);
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
        String outputFile = destinationFile.toString().replace(".txt","-output.txt");
        try {
            long lines = 0;
            try (Stream<String> s = Files.lines(destinationFile)) {
                lines = s.count();
            }
            int cycle = (int) lines/2; //4=2,8=3,16=8
            myWriter = new FileWriter(outputFile);
            for (int i = 0; i < resultMatrices.size(); i=i+cycle) {
                StringBuffer tStr = new StringBuffer();
                StringBuffer bStr = new StringBuffer();
                for (int t = 0; t < cycle; t++) {
                    String[] C = resultMatrices.get(t+i).split(" ");
                    tStr.append(C[0]).append(" ").append(C[1]).append(" ");
                    bStr.append(C[2]).append(" ").append(C[3]).append(" ");
                }
                myWriter.write(tStr.append("\n").toString());
                myWriter.write(bStr.append("\n").toString());

                System.out.println(tStr.toString());
                System.out.println(bStr.toString());

            }
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return(outputFile);

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
