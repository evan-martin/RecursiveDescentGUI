import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import static javax.swing.JOptionPane.showMessageDialog;

public class Main {

    private static Scanner scanner;

    public static void main(String[] args) {

        try {

            JFileChooser j = new JFileChooser("src/tests");
            j.showOpenDialog(null);
            File f = j.getSelectedFile();
            String filepath = f.getPath();
            scanner = new Scanner(new FileReader(filepath));

            String firstLine = scanner.nextLine().trim();
            String initToken = getToken(firstLine);

            if (!initToken.equals("Window")) {
                showMessageDialog(null,"Lexical Error: Required initializing token is \"Window\"");
                return;
            }

            firstLine = firstLine.substring(initToken.length()).trim();

            JFrame frame = makeFrame(firstLine);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } catch (Exception ignored) {
        }
    }

    private static JFrame makeFrame(String firstLine) {

        JFrame frame = new JFrame();
        JPanel contentPane = new JPanel();
        String title;
        ArrayList<Integer> frameSize;

        //set title
        title = getTitle(firstLine);
        firstLine = firstLine.substring(title.length() + 2).trim();
        frame.setTitle(title);

        //set size
        String[] size = firstLine.split("((?<=\\()|(?=\\())|((?<=\\)|(?=\\))))");

        if (!size[0].equals("(") || !size[2].equals(")")) {
            showMessageDialog(null,"Lexical Error: Window size must be enclosed by parentheses");
            System.exit(0);
        } else {
            frameSize = convertDimensions(size[1]);
            if (frameSize.size() != 2) {
                showMessageDialog(null,"Syntax Error: Window dimensions accept two integer parameters");
                System.exit(0);
            } else {
                frame.setSize(frameSize.get(0), frameSize.get(1));
                firstLine = firstLine.substring(firstLine.indexOf(")") + 1).trim();
            }
        }

        //set layout and add content pane
        if (!getToken(firstLine).trim().equals("Layout")) {
            showMessageDialog(null,"Lexical Error: Frame layout must be set, use token \"Layout\"");
            System.exit(0);
        } else {
            firstLine = firstLine.substring(firstLine.indexOf(" ")).trim();
            setLayout(firstLine, contentPane);
            frame.add(contentPane);
        }

        //iterate over top level containers
        String nextLine = scanner.nextLine().trim();
        while (scanner.hasNextLine()) {
            contentPane.add(makeComponent(nextLine));
            nextLine = scanner.nextLine().trim();
            if(getToken(nextLine.trim()).equals("End.")){
                break;
            }
        }

        return frame;
    }

    private static Component makeComponent(String inputLine) {

        String token = getToken(inputLine).trim();

        if (token.equals("Panel")) {
            JPanel panel = new JPanel();
            String layout = inputLine.substring(token.length());

            if (!getToken(layout.trim()).equals("Layout")) {
                showMessageDialog(null,"Lexical Error: Panel layout must be set, use token \"Layout\"");
                return null;
            } else {
                layout = layout.substring(layout.indexOf("t") + 1);
                setLayout(layout.trim(), panel);
                token = scanner.nextLine().trim();
            }

            while (!token.trim().equals("End;")) {
                panel.add(makeComponent(token));
                token = scanner.nextLine().trim();
            }

            return panel;
        }

        if (token.equals("Group")) {
            JPanel group = new JPanel();
            token = scanner.nextLine().trim();

            while (!token.trim().equals("End;")) {
                group.add(makeComponent(token));
                token = scanner.nextLine().trim();
            }
            return group;
        }

        if (token.equals("Textfield")) {
            String widthString = inputLine.substring(token.length());
            ArrayList<Integer> widthArray = convertDimensions(widthString);
            return new JTextField(widthArray.get(0));
        }

        String componentText = inputLine.substring(token.length()).trim();

        if (token.equals("Radio")) {
            return new JRadioButton(getTitle(componentText));
        }

        if (token.equals("Button")) {
            return new JButton(getTitle(componentText));
        }

        if (token.equals("Label")) {
            return new JLabel(getTitle(componentText));
        }

        else {
            showMessageDialog(null,"Lexical Error: invalid token");
            System.exit(0);
            return null;
        }
    }

    private static void setLayout(String token, JPanel panel) {

        if (token.charAt(token.length() - 1) != ':') {
            showMessageDialog(null, "Lexical Error: Layout production must end with : character");
            System.exit(0);
        }
        else if (token.equals("Flow:")) {
            panel.setLayout(new FlowLayout());
        } else {
            String[] gridToken = token.split("((?<=\\()|(?=\\())|((?<=\\)|(?=\\))))");


            if (!gridToken[0].trim().equals("Grid")) {
                showMessageDialog(null,"Lexical Error: Invalid token, expected Flow or Grid");
                System.exit(0);
            } else if (gridToken.length <= 4) {
                showMessageDialog(null,"Lexical Error: Grid dimensions must be enclosed by parentheses");
                System.exit(0);
            } else {

                ArrayList<Integer> gridDimensions = convertDimensions(gridToken[2]);
                if (gridDimensions.size() == 2) {
                    panel.setLayout(new GridLayout(gridDimensions.get(0), gridDimensions.get(1)));
                } else if (gridDimensions.size() == 4) {
                    panel.setLayout(new GridLayout(gridDimensions.get(0), gridDimensions.get(1), gridDimensions.get(2), gridDimensions.get(3)));
                } else {
                    showMessageDialog(null,"Syntax Error: GridLayout takes either 2 or 4 integers as parameters");
                    System.exit(0);
                }
            }
        }
    }

    private static String getToken(String inputLine) {

        String[] token = inputLine.split("\s");

        return token[0];
    }

    private static String getTitle(String inputLine) {

        String[] quote = inputLine.split("((?<=\")|(?=\"))");

        if (quote[0].equals("\"") && quote[1].equals("\"")) {
            return "";
        }

        if (quote.length <= 2) {
            showMessageDialog(null,"Lexical Error: Component names must be enclosed by quotation marks");
            System.exit(0);
        } else if (!quote[0].equals("\"") || !quote[2].equals("\"")) {
            showMessageDialog(null,"Lexical Error: Component names must be enclosed by quotation marks");
            System.exit(0);
        }

        return quote[1];
    }

    private static ArrayList<Integer> convertDimensions(String inputLine) {

        ArrayList<Integer> dimensions = new ArrayList<>();

        inputLine = inputLine.replaceAll("[^0-9]", " ").trim();
        String[] stringArray = inputLine.split("\s");

        for (String s : stringArray) {
            if (!s.equals("")) {
                dimensions.add(Integer.valueOf(s));
            }
        }
        return dimensions;
    }
}