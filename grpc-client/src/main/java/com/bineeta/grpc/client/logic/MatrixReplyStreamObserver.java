package com.bineeta.grpc.client.logic;

import com.bineeta.grpc.server.MatrixReply;
import io.grpc.stub.StreamObserver;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MatrixReplyStreamObserver implements StreamObserver<MatrixReply> {

    private Integer numBlock = 0;
    private Path destinationFile;
    public MatrixReplyStreamObserver(Integer numBlock, Path destinationFile){
        this.numBlock = numBlock;
        this.destinationFile = destinationFile;
    }

    public List<String> resultMatrices = new ArrayList<>();


    @Override
    public void onNext(MatrixReply A) {
        StringBuffer str = new StringBuffer();
        System.out.println("getC00--:"+A.getC00());
        System.out.println("getC01--:"+A.getC01());
        System.out.println("getC10--:"+A.getC10());
        System.out.println("getC11--:"+A.getC11());
        resultMatrices.add(str.append(A.getC00()).append(" ").append(A.getC01())
                .append(" ").append(A.getC10()).append(" ").append(A.getC11()).toString());

    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onCompleted() {


        if (resultMatrices.size()==numBlock){
            System.out.println("---- resultMatrices----"+resultMatrices);
            String str = formResult(destinationFile, resultMatrices);
        }

        //String str = new MultiplicationService().formResult("D:\\localdata\\inputdata", A.resultMatrices);
    }


    private String formResult(Path destinationFile, List<String> resultMatrices) {
        FileWriter myWriter = null;
        StringBuffer str = new StringBuffer();
        String outputFile = destinationFile.toString().replace(".txt", "-output.txt");
        try {
            int cycle = (int) numBlock / 2; //4=2,8=3,16=8
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





}