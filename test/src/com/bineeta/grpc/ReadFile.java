package com.bineeta.grpc;

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

public class ReadFile {

    public static void main(String args[]) {
        //String filename = "D:\\localdata\\inputdata\\matrix.txt";
        String filename = "D:\\localdata\\inputdata\\matrix-large.txt";
        //String filename = "D:\\localdata\\inputdata\\matrix-error.txt";
        //String filename = "D:\\localdata\\inputdata\\matrix-error-1.txt";

        Path destinationFile = new File(filename).toPath();
        System.out.println("destinationFile: "+destinationFile.toString().replace(".txt","-output.txt"));
 //       Path destinationFile = new File(filename).toPath();


        //System.out.println(isValidFile(filename));
//        if (isValidFile(filename)) {
//            List<List<Integer>> splitMatrix = getSplitMatrices(filename);
//
//        }
//        List<String> result = new ArrayList<String>() {
//            {
//                add("1 2 3 4");
//                add("5 6 7 8");
//                add("9 10 11 12");
//                add("13 14 15 16");
//                add("17 18 19 20");
//                add("21 22 23 24");
//                add("25 26 27 28");
//                add("29 30 31 32");
//                add("33 34 35 36");
//                add("37 38 39 40");
//                add("41 42 43 44");
//                add("45 46 47 48");
//                add("49 50 51 52");
//                add("53 54 55 56");
//                add("57 58 59 60");
//                add("61 62 63 64");
//                add("65 66 67 68");
//                add("69 70 71 72");
//                add("73 74 75 76");
//                add("77 78 79 80");
//                add("81 82 83 84");
//                add("85 86 87 88");
//                add("89 90 91 92");
//                add("93 94 95 96");
//                add("97 98 99 100");
//                add("101 102 103 104");
//                add("105 106 107 108");
//                add("109 110 111 112");
//                add("113 114 115 116");
//                add("117 118 119 120");
//                add("121 122 123 124");
//                add("125 126 127 128");
//                add("129 130 131 132");
//                add("133 134 135 136");
//                add("137 138 139 140");
//                add("141 142 143 144");
//                add("145 146 147 148");
//                add("149 150 151 152");
//                add("153 154 155 156");
//                add("157 158 159 160");
//                add("161 162 163 164");
//                add("165 166 167 168");
//                add("169 170 171 172");
//                add("173 174 175 176");
//                add("177 178 179 180");
//                add("181 182 183 184");
//                add("185 186 187 188");
//                add("189 190 191 192");
//                add("193 194 195 196");
//                add("197 198 199 200");
//                add("201 202 203 204");
//                add("205 206 207 208");
//                add("209 210 211 212");
//                add("213 214 215 216");
//                add("217 218 219 220");
//                add("221 222 223 224");
//                add("225 226 227 228");
//                add("229 230 231 232");
//                add("233 234 235 236");
//                add("237 238 239 240");
//                add("241 242 243 244");
//                add("245 246 247 248");
//                add("249 250 251 252");
//                add("253 254 255 256");
//
//            }
//        };
//        System.out.println(result);
//
//        File file = new File("D:\\localdata\\outputdata\\out.txt");
//        FileWriter myWriter = null;
//        long lines = 16 ;
//        int cycle = (int) lines/2; //4=2,8=3,16=8
//        System.out.println(cycle);
//        StringBuffer str = new StringBuffer();
//        try {
//            myWriter = new FileWriter(file);
//            for (int i = 0; i < result.size(); i=i+cycle) {
//                StringBuffer tStr = new StringBuffer();
//                StringBuffer bStr = new StringBuffer();
//                for (int t = 0; t < cycle; t++) {
//                    String[] C = result.get(t+i).split(" ");
//                    tStr.append(C[0]).append(" ").append(C[1]).append(" ");
//                    bStr.append(C[2]).append(" ").append(C[3]).append(" ");
//                }
//                myWriter.write(tStr.append("\n").toString());
//                myWriter.write(bStr.append("\n").toString());
//
//                System.out.println(tStr.toString());
//                System.out.println(bStr.toString());
//
//            }
//            myWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static List<List<Integer>> getSplitMatrices(String filename) {
        List<List<Integer>> splitMatrices = new ArrayList<>();
        File f = new File(filename);
        long lines = 0;
        //int[][] input = null;
        try (Stream<String> s = Files.lines(f.toPath())) {
            lines = s.count();
            System.out.println("lines" + lines);

            List<List<Integer>> matrix = new ArrayList<>();
            try (Stream<String> s1 = Files.lines(f.toPath())) {
                List<String[]> data = s1.map(l -> l.split(" "))
                        .collect(Collectors.toList());
                matrix = data.stream().map(l -> method(l)).collect(Collectors.toList());
            }


            for (int i = 0; i < lines; i = i + 2) {
                for (int j = 0; j < lines; j = j + 2) {
                    System.out.println("\ni-j" + i + "-" + j);
                    List<Integer> smallMatrix = new ArrayList<>();
                    int a = i, b = j;
                    smallMatrix.add(matrix.get(a).get(b));
                    smallMatrix.add(matrix.get(a).get(b + 1));
                    smallMatrix.add(matrix.get(a + 1).get(b));
                    smallMatrix.add(matrix.get(a + 1).get(b + 1));
                    splitMatrices.add(smallMatrix);
                }
            }
            System.out.println(splitMatrices);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return splitMatrices;
    }

    private static List<Integer> method(String[] l) {
        List<Integer> x = Arrays.stream(l).map(num -> Integer.parseInt(num)).collect(Collectors.toList());
        return x;

    }

    private static boolean isValidFile(String filename) {
        try {
            File f = new File(filename);
            long lines = 0;
            try (Stream<String> s = Files.lines(f.toPath())) {
                lines = s.count();
            }
            if (isPowerOf2(lines)) {
                long finalLines = lines;
                Predicate<String> isInValid = i -> !isPowerOf2(i.split(" ").length) && i.split(" ").length != finalLines;
                Optional opt = null;
                try (Stream<String> s = Files.lines(f.toPath())) {
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


    private static boolean isPowerOf2(long number) {
        System.out.println((Math.ceil((Math.log(number) / Math.log(2)))));
        System.out.println((Math.floor((Math.log(number) / Math.log(2)))));
        return (int) (Math.ceil((Math.log(number) / Math.log(2))))
                == (int) (Math.floor(((Math.log(number) / Math.log(2)))));
    }


}
