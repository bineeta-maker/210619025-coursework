package com.bineeta.grpc.client.logic.utils;

import com.bineeta.grpc.server.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;

public class MulitplicationUtils {

    public List<MatrixServiceGrpc.MatrixServiceBlockingStub> getAvailableChannelBlockingStubs() {
        List<MatrixServiceGrpc.MatrixServiceBlockingStub> availableStubs = new ArrayList<>();

        ManagedChannel channel1 = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext().build();

        ManagedChannel channel2 = ManagedChannelBuilder.forAddress("localhost", 9092)
                .usePlaintext().build();
        ManagedChannel channel3 = ManagedChannelBuilder.forAddress("localhost", 9093)
                .usePlaintext().build();
        MatrixServiceGrpc.MatrixServiceBlockingStub stub1
                = MatrixServiceGrpc.newBlockingStub(channel1);
        MatrixServiceGrpc.MatrixServiceBlockingStub stub2
                = MatrixServiceGrpc.newBlockingStub(channel2);
        MatrixServiceGrpc.MatrixServiceBlockingStub stub3
                = MatrixServiceGrpc.newBlockingStub(channel3);

        availableStubs.add(stub1);
        availableStubs.add(stub2);
        availableStubs.add(stub3);
        return availableStubs;

    }

    public List<MatrixServiceGrpc.MatrixServiceStub> getAvailableChannelStubs() {

        List<MatrixServiceGrpc.MatrixServiceStub> availableStubs = new ArrayList<>();

        ManagedChannel channel1 = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext().build();
        ManagedChannel channel2 = ManagedChannelBuilder.forAddress("localhost", 9092)
                .usePlaintext().build();
        ManagedChannel channel3 = ManagedChannelBuilder.forAddress("localhost", 9093)
                .usePlaintext().build();
        MatrixServiceGrpc.MatrixServiceStub stub1
                = MatrixServiceGrpc.newStub(channel1);
        MatrixServiceGrpc.MatrixServiceStub stub2
                = MatrixServiceGrpc.newStub(channel2);
        MatrixServiceGrpc.MatrixServiceStub stub3
                = MatrixServiceGrpc.newStub(channel3);

        availableStubs.add(stub1);
        availableStubs.add(stub2);
        availableStubs.add(stub3);
        return availableStubs;

    }


}
