package com.bineeta.grpc.client.logic.utils;

import com.bineeta.grpc.server.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;

public class MulitplicationUtils {

    public List<MatrixServiceGrpc.MatrixServiceBlockingStub> getAvailableChannelBlockingStubs() {
        List<MatrixServiceGrpc.MatrixServiceBlockingStub> availableStubs = new ArrayList<>();

        ManagedChannel channel1 = ManagedChannelBuilder.forAddress("35.188.202.145", 9090)
                .usePlaintext().build();
        ManagedChannel channel2 = ManagedChannelBuilder.forAddress("34.67.205.179", 9090)
                .usePlaintext().build();
        ManagedChannel channel3 = ManagedChannelBuilder.forAddress("23.251.152.76", 9090)
                .usePlaintext().build();
        ManagedChannel channel4 = ManagedChannelBuilder.forAddress("34.135.0.141", 9090)
                .usePlaintext().build();
        ManagedChannel channel5 = ManagedChannelBuilder.forAddress("34.72.235.17", 9090)
                .usePlaintext().build();
        ManagedChannel channel6 = ManagedChannelBuilder.forAddress("34.134.148.1", 9090)
                .usePlaintext().build();
        ManagedChannel channel7 = ManagedChannelBuilder.forAddress("35.239.90.27", 9090)
                .usePlaintext().build();
        ManagedChannel channel8 = ManagedChannelBuilder.forAddress("35.184.205.152", 9090)
                .usePlaintext().build();

        MatrixServiceGrpc.MatrixServiceBlockingStub stub1
                = MatrixServiceGrpc.newBlockingStub(channel1);
        MatrixServiceGrpc.MatrixServiceBlockingStub stub2
                = MatrixServiceGrpc.newBlockingStub(channel2);
        MatrixServiceGrpc.MatrixServiceBlockingStub stub3
                = MatrixServiceGrpc.newBlockingStub(channel3);
        MatrixServiceGrpc.MatrixServiceBlockingStub stub4
                = MatrixServiceGrpc.newBlockingStub(channel4);
        MatrixServiceGrpc.MatrixServiceBlockingStub stub5
                = MatrixServiceGrpc.newBlockingStub(channel5);
        MatrixServiceGrpc.MatrixServiceBlockingStub stub6
                = MatrixServiceGrpc.newBlockingStub(channel6);
        MatrixServiceGrpc.MatrixServiceBlockingStub stub7
                = MatrixServiceGrpc.newBlockingStub(channel7);
        MatrixServiceGrpc.MatrixServiceBlockingStub stub8
                = MatrixServiceGrpc.newBlockingStub(channel8);

        availableStubs.add(stub1);
        availableStubs.add(stub2);
        availableStubs.add(stub3);
        availableStubs.add(stub4);
        availableStubs.add(stub5);
        availableStubs.add(stub6);
        availableStubs.add(stub7);
        availableStubs.add(stub8);

        return availableStubs;

    }

    public List<MatrixServiceGrpc.MatrixServiceStub> getAvailableChannelStubs() {

        List<MatrixServiceGrpc.MatrixServiceStub> availableStubs = new ArrayList<>();

        ManagedChannel channel1 = ManagedChannelBuilder.forAddress("35.188.202.145", 9090)
                .usePlaintext().build();
        ManagedChannel channel2 = ManagedChannelBuilder.forAddress("34.67.205.179", 9090)
                .usePlaintext().build();
        ManagedChannel channel3 = ManagedChannelBuilder.forAddress("23.251.152.76", 9090)
                .usePlaintext().build();
        ManagedChannel channel4 = ManagedChannelBuilder.forAddress("34.135.0.141", 9090)
                .usePlaintext().build();
        ManagedChannel channel5 = ManagedChannelBuilder.forAddress("34.72.235.17", 9090)
                .usePlaintext().build();
        ManagedChannel channel6 = ManagedChannelBuilder.forAddress("34.134.148.1", 9090)
                .usePlaintext().build();
        ManagedChannel channel7 = ManagedChannelBuilder.forAddress("35.239.90.27", 9090)
                .usePlaintext().build();
        ManagedChannel channel8 = ManagedChannelBuilder.forAddress("35.184.205.152", 9090)
                .usePlaintext().build();

        MatrixServiceGrpc.MatrixServiceStub stub1
                = MatrixServiceGrpc.newStub(channel1);
        MatrixServiceGrpc.MatrixServiceStub stub2
                = MatrixServiceGrpc.newStub(channel2);
        MatrixServiceGrpc.MatrixServiceStub stub3
                = MatrixServiceGrpc.newStub(channel3);
        MatrixServiceGrpc.MatrixServiceStub stub4
                = MatrixServiceGrpc.newStub(channel4);
        MatrixServiceGrpc.MatrixServiceStub stub5
                = MatrixServiceGrpc.newStub(channel5);
        MatrixServiceGrpc.MatrixServiceStub stub6
                = MatrixServiceGrpc.newStub(channel6);
        MatrixServiceGrpc.MatrixServiceStub stub7
                = MatrixServiceGrpc.newStub(channel7);
        MatrixServiceGrpc.MatrixServiceStub stub8
                = MatrixServiceGrpc.newStub(channel8);

        availableStubs.add(stub1);
        availableStubs.add(stub2);
        availableStubs.add(stub3);
        availableStubs.add(stub4);
        availableStubs.add(stub5);
        availableStubs.add(stub6);
        availableStubs.add(stub7);
        availableStubs.add(stub8);

        return availableStubs;

    }


}
