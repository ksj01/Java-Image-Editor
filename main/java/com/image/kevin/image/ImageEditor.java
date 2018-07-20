package com.image.kevin.image;
import java.util.Scanner;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ImageEditor {
    public static int width;
    public static int height;
    public static int maxVal;
    public static int[][][] pixels;

    public static void main(String[] args) {
        ImageEditor image = new ImageEditor();
        String file = args[0];
        String output = args[1];
        String style = args[2];
        int blurVal = 0;
        if(args.length == 4) {
            try {
                blurVal = Integer.parseInt(args[3]);
            }
            catch(NumberFormatException e) {
            }
        }
        image.processFile(file);

        if (style.equals("invert")) {
            image.invert();
        }
        else if (style.equals("grayscale")) {
            image.grayscale();
        }
        else if (style.equals("emboss")) {
            image.emboss();
        }
        else if (style.equals("motionblur")) {

            if (blurVal <= 0) {
                System.out.println("No valid MotionBlur value. Please try again by specifying a blur value of greater than 0");
                System.exit(1);
            }
            else {
                image.motion(blurVal);
            }
        }

        else {
            System.out.println("USAGE: java ImageEditor in-file out-file (grayscale|invert|emboss|motionblur motion-blur-length)");
            System.exit(1);
        }
        if (output != null) {
            image.output(output);
        }
        else {
            image.output(file);
        }
    }

    public void processFile(String file) {

        String commentReg = "#[^\\n]*";


        try {
            StringBuilder tempString = new StringBuilder();
            FileReader loadedFile = new FileReader(file);
            Scanner sc = new Scanner(loadedFile);
            Pattern comment = Pattern.compile(commentReg);
            while(sc.hasNext()) {
                    String current = sc.next();
                    if (current.charAt(0) != '#') {
                        Matcher match = comment.matcher(current);
                        if (!match.find()) {
                            tempString.append(current + '\n');
                        } else {
                            String[] parts = current.split("#");
                            tempString.append(parts[0] + '\n');
                        }
                    }
                    else {
                        sc.nextLine();
                    }
            }
            sc.close();
            Scanner store = new Scanner(tempString.toString());
            store.next();
            ImageEditor.width = Integer.parseInt(store.next());
            ImageEditor.height = Integer.parseInt(store.next());
            ImageEditor.maxVal = Integer.parseInt(store.next());
            ImageEditor.pixels = new int[height][width][3];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y][x][0] = Integer.parseInt(store.next()); //red
                    pixels[y][x][1] = store.nextInt(); //green
                    pixels[y][x][2] = Integer.parseInt(store.next()); //blue
                }
            }


            loadedFile.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + file + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '" + file + "'"
            );
        }
    }

    public void invert() {
        for(int y = 0; y < ImageEditor.height; y++) {
            for(int x = 0; x < ImageEditor.width; x++) {
                ImageEditor.pixels[y][x][0] = ImageEditor.maxVal - ImageEditor.pixels[y][x][0];
                ImageEditor.pixels[y][x][1] = ImageEditor.maxVal - ImageEditor.pixels[y][x][1];
                ImageEditor.pixels[y][x][2] = ImageEditor.maxVal - ImageEditor.pixels[y][x][2];
            }
        }
    }

    public void grayscale() {
        for(int y = 0; y < ImageEditor.height; y++) {
            for(int x = 0; x < ImageEditor.width; x++) {
                ImageEditor.pixels[y][x][0] = (ImageEditor.pixels[y][x][0] + ImageEditor.pixels[y][x][1] + ImageEditor.pixels[y][x][2]) / 3;
                ImageEditor.pixels[y][x][1] = ImageEditor.pixels[y][x][0];
                ImageEditor.pixels[y][x][2] = ImageEditor.pixels[y][x][0];
            }
        }
    }

    public void emboss() {
        for(int y = ImageEditor.height - 1; y >= 0; y--) {
            for(int x = ImageEditor.width - 1; x >= 0; x--) {
                int V;
                if (!(x == 0 || y == 0)) {
                    int redDiff = ImageEditor.pixels[y][x][0] - ImageEditor.pixels[y - 1][x - 1][0];
                    int greenDiff = ImageEditor.pixels[y][x][1] - ImageEditor.pixels[y - 1][x - 1][1];
                    int blueDiff = ImageEditor.pixels[y][x][2] - ImageEditor.pixels[y - 1][x - 1][2];
                    int highest = 0;

                    if ((Math.abs(redDiff) >= Math.abs(greenDiff)) && Math.abs(redDiff) >= Math.abs(blueDiff)) {
                        highest = redDiff;
                    }
                    else if ((Math.abs(greenDiff) > Math.abs(redDiff)) && (Math.abs(greenDiff) >= (Math.abs(blueDiff)))) {
                        highest = greenDiff;
                    }
                    else if ((Math.abs(blueDiff) > Math.abs(redDiff)) && (Math.abs(blueDiff) > (Math.abs(greenDiff)))) {
                        highest = blueDiff;
                    }

                    V = 128 + highest;
                    if (V > 255) {
                        V = 255;
                    }
                    else if (V < 0) {
                        V = 0;
                    }
                }
                else {
                    V = 128;
                }
                ImageEditor.pixels[y][x][0] = V;
                ImageEditor.pixels[y][x][1] = V;
                ImageEditor.pixels[y][x][2] = V;

            }
        }
    }

    public void motion(int blurVal) {
        for(int y = 0; y < ImageEditor.height; y++) {
            for(int x = 0; x < ImageEditor.width; x++) {
                int maxWidth = 1;
                for( int z = 1; z < blurVal; z++) {
                    if((x + z) < ImageEditor.width) {
                        ImageEditor.pixels[y][x][0] += ImageEditor.pixels[y][x + z][0];
                        ImageEditor.pixels[y][x][1] += ImageEditor.pixels[y][x + z][1];
                        ImageEditor.pixels[y][x][2] += ImageEditor.pixels[y][x + z][2];
                        maxWidth++;
                    }
                }
                ImageEditor.pixels[y][x][0] /= maxWidth;
                ImageEditor.pixels[y][x][1] /= maxWidth;
                ImageEditor.pixels[y][x][2] /= maxWidth;
            }
        }
    }

    public void output(String file) {
        try {
            PrintWriter output = new PrintWriter(new FileWriter(file));
            output.println("P3");
            output.println(ImageEditor.width + " " + ImageEditor.height);
            output.println(ImageEditor.maxVal);
            for(int y = 0; y < ImageEditor.height; y++) {
                for(int x = 0; x < ImageEditor.width; x++) {
                    output.println(ImageEditor.pixels[y][x][0]);
                    output.println(ImageEditor.pixels[y][x][1]);
                    output.println(ImageEditor.pixels[y][x][2]);
                }
            }
            output.flush();
            output.close();
        }

        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + file + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error printing to file '" + file + "'"
            );
        }
    }

}
