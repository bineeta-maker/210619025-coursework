package com.bineeta.grpc.client.logic;

import com.bineeta.grpc.server.MatrixReply;
import com.bineeta.grpc.server.MatrixRequest;
import com.bineeta.grpc.server.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class MatrixReplyStreamObserver implements StreamObserver<MatrixReply> {

    private Integer numMultipliedBlock = 0;
    private Integer splitMatrixSize = 0;
    private Path destinationFile;
    private Boolean isTest;

    private CountDownLatch cd = null;

    public MatrixReplyStreamObserver(Integer splitMatrixSize, Path destinationFile, Boolean isTest) {
        this.numMultipliedBlock = (int) (splitMatrixSize * Math.sqrt(splitMatrixSize));
        this.splitMatrixSize = splitMatrixSize;
        this.destinationFile = destinationFile;
        this.isTest = isTest;
        this.cd = new CountDownLatch(numMultipliedBlock);
    }

    private int countdown = 0;
    public List<MatrixReply> resultMatrices = new ArrayList<>();


    @Override
    public void onNext(MatrixReply A) {
        StringBuffer str = new StringBuffer();
        resultMatrices.add(A);
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onCompleted() {
        if (isTest) {
            cd.countDown();
            //System.out.println("onCompleted resultMatrices--: " + resultMatrices);
            if (cd.getCount() == 0) {
                pushToAddBlock();
            }
        }
    }

    private void pushToAddBlock() {
        List<MatrixReply> finalAnswer = new ArrayList<>();
        Map<Integer, List<MatrixReply>> groupedResultMatrices =
                resultMatrices.stream().collect(Collectors.groupingBy(w -> w.getId()));

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        MatrixServiceGrpc.MatrixServiceBlockingStub stub
                = MatrixServiceGrpc.newBlockingStub(channel);

        for (Map.Entry<Integer, List<MatrixReply>> entry : groupedResultMatrices.entrySet()) {
            int size = entry.getValue().size();
            int blockNumber = entry.getKey();
            List<MatrixReply> blocks = entry.getValue();
            MatrixReply added = MatrixReply.newBuilder()
                    .setC00(0).setC01(0).setC10(0).setC11(0).build();
            for (int i = 1; i <= size; i++) {
                // keep on adding for each row and column
                MatrixReply A = blocks.get(i - 1);
                added = stub.addBlock(MatrixRequest.newBuilder().setId(blockNumber + 1)//First Result Block Calculation
                        .setA00(added.getC00()).setA01(added.getC01()).setA10(added.getC10()).setA11(added.getC11())
                        .setB00(A.getC00()).setB01(A.getC01()).setB10(A.getC10()).setB11(A.getC11()).build());
            }
            finalAnswer.add(added);
        }

        finalAnswer.sort(Comparator.comparing(o -> ((Integer) o.getId())));
        formResult(destinationFile, finalAnswer);
    }


    private String formResult(Path destinationFile, List<MatrixReply> finalAnswer) {
        StringBuffer str = new StringBuffer();
        String outputFile = destinationFile.toString().replace(".txt", "-output.txt");
        System.out.println("outputFile----" + outputFile);
        try {
            int cycle = (int) Math.sqrt(splitMatrixSize); //4=2,16=4
            FileWriter myWriter = new FileWriter(new File(outputFile));
            for (int i = 0; i < finalAnswer.size(); i = i + cycle) {
                StringBuffer tStr = new StringBuffer();
                StringBuffer bStr = new StringBuffer();
                for (int t = 0; t < cycle; t++) {
                    //if (resultMatrices.get(0))
                    MatrixReply C = finalAnswer.get(t + i);
                    tStr.append(C.getC00()).append(" ").append(C.getC01()).append(" ");
                    bStr.append(C.getC10()).append(" ").append(C.getC11()).append(" ");
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


}