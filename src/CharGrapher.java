// For basic needs
import java.io.*;
import java.awt.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Iterator;

// For photo process
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.*;
import java.awt.image.BufferedImage;
import java.awt.color.ColorSpace;

// For the Window and the elements
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.awt.font.*;

class CharGrapher extends JFrame implements ActionListener
{
    static final long serialVersionUID = 0021L;
    static final String ver = "1.2.0 Preview";
    // Initialize some objects that are related to the functions.
    static JPanel pnlObj = new JPanel();
    static BufferedWriter bw;
    static FileReader fr;
    static Process pylaunch;

    public static BufferedImage img, resizedImg, jpgImg;
    static String input, fileExtension, fileName, cuScaleChar, str, opFileName1 = "";
    static String ssgWS = new File("").getAbsoluteFile().toString() + "/CharGrapherWorkSpace/";
    static String[] helpTips = {
        "Start the process by clicking here.",                                                          // 0
        "It's where the output will be. It's editable",                                                 // 1
        "Input your file path here. <required>",                                                        // 2
        "Output to a txt file als You don't need to specify the txt name. <optional>",                  // 3
        "Select a converting mode to continue. <required>",                                             // 4
        "Reset all the field in this window to their default.",                                         // 5
        "The variaties of characters included in the output text."+                                     
        "'5' means keep origin. '0' means output only contains two characters."+
        " ($ and <space>). It's interactive in Camera --> CharGraph mode. <optional>",                   // 6
        "Select a font for the words that will be displayed. <required>",                               // 7
        "The resolution(dimension) of the outputted graph. By default,"+ 
        "it's 20000 pixels, which is also the maximum degree. It's interactive in Cam-->CharGraph mode.",// 8
        "Input your characters here. NOTE:']' represents output in a seperate line. "+
        "Maximum is 10 characters at once. The exceeding parts will be ignored. <required>"             // 9
    };
    static String[] modes = {
        "Photo --> CharGraph",       // 0
        "Words --> CharGraph",       // 1
        "Camera --> CharGraph",      // 2
        "Photo --> Hexadecimal"      // 3 *NEW*
    };
    static String[] labels = {
        "Photo Path:",      // 0
        "Complexity:",      // 1
        "Characters:",      // 2
        "Resolution:",      // 3
        "Font Size:",       // 4
        "Characters Font:"  // 5
    };
    static String[] btns = {
        "Start",              // 0
        "Reset Field",        // 1
        "Output To Txt",      // 2
        "Pause",              // 3
        "Continue",           // 4
        "Color Reverse",      // 5
        "Reverse Back"        // 6
    };
    static String[] scales = {
        "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$                                         ",     // 0
        "WWWWWWWWWWWWWWWCCCCCCCCCCCCCCCrrrrrrrrrr{{{{{{{{{{>>>>>>>>>>``````````            ",     // 1
        "&&&&&WWWWWdddddpppppCCCCCJJJJJxxxxxrrrrr11111{{{{{<<<<<>>>>>^^^^^`````            ",     // 2  
        "%%%%8&WWWWkkkkbdppppQQQQLCJJJJuuuunxrrrr(((()1{{{{++++~<>>>>,,,,\"^`````           ",    // 3
        "BBB%8&WMMMhhhkbdpqqq000QLCJUUUvvvunxrjjj|||()1{}}}___+~<>iii:::,\"^''''`           ",    // 4
        "@@B%8&WM##aahkbdpqwwOO0QLCJUYYccvunxrjff\\\\|()1{}[[--_+~<>i!!;;:,\"^`'.             ",  // 5
        "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'..            ",   // 6
    };

    static int imgWidth, imgHeight, imgResolution;
    static boolean pyhasdestroid = false;

    static boolean ifJpg = false;
    static Scanner scr = new Scanner(System.in);


    /* This is maximum Resolution in which you want the output to hold. You can adjust but it's hoped not 
    to be too large otherwise the text will be overflowed in the output area. Of course, the bigger the
    better. But if resolution from photos that later inputted is larger than this, it will be compressed
    to this. 
    NOTICE : This must set to be a decimal, which is expected to adhere a .0 at last, but not required.*/
    static double maxResolution, sliderRes = 20000.0;

    /* This is the switch to open the function that cut up the space parts in the front part of the output.
    Even though this is recommended to trun on every time and very useful, for some very very special 
    circumstances, the improvement doesn't work and would probably distort the original photo, or, user 
    doesn't want to use this function. You can change it to false if you want.    
    NOTICE : This function is only applicable in Photo --> CharGraph mode.                               */
    static boolean isCutUpSpacePart = true;

    // Elements in the window
    static JButton startBtn = new JButton(btns[0]);
    static JButton resetBtn = new JButton(btns[1]);
    static JButton optxtBtn = new JButton(btns[2]);
    static JButton reverseBtn = new JButton(btns[5]);

    static JTextField stringInputField = new JTextField(20); // For file path
    static JTextField charInputField = new JTextField(10); // For characters
    static JTextArea txtOutput = new JTextArea(120, 270);

    static JLabel inputLable = new JLabel(labels[0]);
    static JLabel sliderLable = new JLabel(labels[1]);
    static JLabel sliderLable2 = new JLabel(labels[3]);
    static JLabel fontLable = new JLabel(labels[5]);

    static Font txtOutputFont = new Font("Courier New", Font.PLAIN, 5), charFont;

    static JComboBox<String> modeBox = new JComboBox<String>(modes);
    static JComboBox<String> fontBox = new JComboBox<String>
    (GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

    static JSlider wordComplexitySlider = new JSlider(JSlider.HORIZONTAL, 0, 6, 6);
    static JSlider resolutionSlider = new JSlider(JSlider.HORIZONTAL, 2,
     (int) sliderRes, (int) sliderRes);

    static JScrollPane areaScrollPane = new JScrollPane(txtOutput);


    // Constructor for the Window
    public CharGrapher()
    {
        super("Sharp String Grapher "+ver);

        // Hit Enter to process. This area is only for Photo --> CharGraph mode
        stringInputField.addKeyListener(new KeyListener(){
            public void keyPressed (KeyEvent e) {}
            public void keyTyped (KeyEvent e) {}
            public void keyReleased (KeyEvent ev)
            {
                // 10 represents Enter. Thus, if user hits enter, then process began
                if(ev.getKeyCode() == 10)
                {
                    try {
                        warninfo(stringInputField.getText());
                        photoToGraph(stringInputField.getText());
                    } catch (IOException e) {}
                }
                return;
            }
        });

        startBtn.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent ev) 
            {
                txtOutput.setText("");
                // If it's Photo --> CharGraph mode
                if (modeBox.getSelectedItem().toString().equals(modes[0])) {
                    try{
                        photoToGraph(stringInputField.getText());
                    } catch(IOException e){}
                    return;
                }

                // If it's Characters --> CharGraph mode
                if(modeBox.getSelectedItem().toString().equals(modes[1]))
                {
                    // Direct to Character --> CharGraph method.
                    charToGraph();
                    return;
                }

                // If it's Camera --> CharGraph mode
                if(modeBox.getSelectedItem().toString().equals(modes[2]))
                {
                    // If the status is 'paused'
                    if(startBtn.getText().equals(btns[3]))
                    {
                        pydestroy();
                        startBtn.setText(btns[4]);
                    } else { // If the status is 'running'
                        // Kill the previous-launcged Thread
                        pydestroy();
                        // If the button printed "continue"
                        startBtn.setText(btns[3]);
                        camModeProcess();
                    }
                    return;
                }
                
                // If it's Photo --> CharGraph mode
                if(modeBox.getSelectedItem().toString().equals(modes[3]))
                {
                    photoToHex(stringInputField.getText());
                    return;
                }
            }
        });

        resetBtn.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                // Reset all the fields to their default status
                charInputField.setText("");
                stringInputField.setText("");
                txtOutput.setText("");
                wordComplexitySlider.setValue(6);
                resolutionSlider.setValue((int)maxResolution);
                if (reverseBtn.getText().equals(btns[6])) {
                    // Simulate a clicking.
                    reverseBtn.doClick();
                }
                resolutionSlider.setValue(resolutionSlider.getMaximum());
                return;
            }
        });

        resolutionSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // If it's Characters --> CharGraph mode, exit the method
                if(modeBox.getSelectedItem().toString().equals(modes[1])) return;
                // Set max resolution
                maxResolution = (double) resolutionSlider.getValue();
            }
        });

        wordComplexitySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // Inform the method to change the value
                cuScaleChar =  CharGrapher.getCusScale();
            }
        });

        reverseBtn.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                // Set the text of Button and change
                if(reverseBtn.getText() == btns[5]) {
                    reverseBtn.setText(btns[6]);
                } else {
                    reverseBtn.setText(btns[5]);
                }
                // Read each characters from back and put on the top front.
                for(int i = 0; i < scales.length; i++) {
                    String scales_temp = "";
                    for(int j=1; j <= scales[i].length(); j++) {
                        scales_temp += scales[i].substring(scales[i].length()-j,scales[i].length()-j+1);
                    }
                    scales[i] = scales_temp;
                }
                String scales_temp = "";
                for(int i=1; i <= cuScaleChar.length(); i++) {
                    scales_temp += cuScaleChar.substring(cuScaleChar.length()-i, cuScaleChar.length()-i+1);
                }
                cuScaleChar = scales_temp;
            }
        });

        optxtBtn.addActionListener(new ActionListener(){
            public void actionPerformed (ActionEvent e)
            {
                if(!outputToTxt())
                    errinfo("Software is unable to output to txt file. Either because" +
                    "the output field is blank or the it doesn't have right to write");
            }
        });

        // Select Mode
        modeBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e)
            {
                // If change is detected
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    // Photo --> CharGraph mode
                    if(modeBox.getSelectedItem().toString().equals(modes[0]))
                    {
                        // Remove previously written data
                        txtOutput.setText("");
                        // Destroy previous launched python script.
                        pydestroy();
                        // Reconstructing GUI
                        inputLable.setVisible(true);
                        stringInputField.setVisible(true);
                        charInputField.setVisible(false);
                        sliderLable.setVisible(true);
                        wordComplexitySlider.setVisible(true);
                        fontLable.setVisible(false);
                        fontBox.setVisible(false);
                        startBtn.setText(btns[0]);
                        sliderLable2.setText(labels[3]);
                        resolutionSlider.setMinimum(0);
                        resolutionSlider.setMaximum((int) sliderRes);
                        resolutionSlider.setValue(resolutionSlider.getMaximum());
                        resolutionSlider.setMajorTickSpacing(1);
                        resolutionSlider.setSnapToTicks(false);
                        resolutionSlider.setPaintTicks(false);

                        inputLable.setText(labels[0]);
                    }
                    
                    // Char --> CharGraph mode
                    if(modeBox.getSelectedItem().toString().equals(modes[1]))
                    {
                        // Remove previously written data
                        txtOutput.setText("");
                        // Destroy previous launched python script.
                        pydestroy();
                        // Reconstructing GUI
                        inputLable.setVisible(true);
                        stringInputField.setVisible(true);
                        stringInputField.setVisible(false);
                        charInputField.setVisible(true);
                        sliderLable.setVisible(false);
                        wordComplexitySlider.setVisible(false);
                        fontLable.setVisible(true);
                        fontBox.setVisible(true);
                        startBtn.setText(btns[0]);
                        sliderLable2.setText(labels[4]);
                        resolutionSlider.setMinimum(5);
                        resolutionSlider.setMaximum(70);
                        resolutionSlider.setValue(resolutionSlider.getMaximum());
                        resolutionSlider.setMajorTickSpacing(4);
                        resolutionSlider.setSnapToTicks(true);
                        resolutionSlider.setPaintTicks(true);

                        inputLable.setText(labels[2]);
                    }
                    
                    // Cam --> CharGraph mode
                    if(modeBox.getSelectedItem().toString().equals(modes[2]))
                    {   
                        // Remove previously written data
                        txtOutput.setText("");
                        // Destroy previous launched python script.
                        pydestroy();
                        // Reconstructing GUI
                        inputLable.setVisible(false);
                        stringInputField.setVisible(false);
                        charInputField.setVisible(false);
                        sliderLable.setVisible(true);
                        wordComplexitySlider.setVisible(true);
                        fontLable.setVisible(false);
                        fontBox.setVisible(false);
                        sliderLable2.setText(labels[3]);
                        resolutionSlider.setMinimum(20);
                        resolutionSlider.setMaximum((int)sliderRes);
                        resolutionSlider.setValue(resolutionSlider.getMaximum());
                        resolutionSlider.setMajorTickSpacing(1);
                        resolutionSlider.setSnapToTicks(false);
                        resolutionSlider.setPaintTicks(false);

                        startBtn.setText(btns[3]);
                        // Lauch the multithread.
                        if(!buildPy())
                        {
                            errinfo("Sorry, python script building failed."+ 
                            "Please see 'readme.md' for further instruction");
                            return; // If building failed, stop building.
                        }
                        camModeProcess();
                    }

                    if(modeBox.getSelectedItem().toString().equals(modes[3]))
                    {
                        errinfo("Sorry, this function is still in developing");
                        modeBox.setSelectedIndex(0);
                        return;
                        // // Remove previously written data
                        // txtOutput.setText("");
                        // // Destroy previous launched python script.
                        // pydestroy();
                        // // Reconstructing GUI
                        // inputLable.setVisible(true);
                        // stringInputField.setVisible(true);
                        // charInputField.setVisible(false);
                        // sliderLable.setVisible(false);
                        // wordComplexitySlider.setVisible(false);
                        // fontLable.setVisible(false);
                        // fontBox.setVisible(false);
                        // sliderLable2.setText(labels[3]);

                        // resolutionSlider.setMinimum(0);
                        // resolutionSlider.setMaximum((int)sliderRes);
                        // resolutionSlider.setValue(resolutionSlider.getMaximum());
                        // resolutionSlider.setMajorTickSpacing(1);
                        // resolutionSlider.setSnapToTicks(false);
                        // resolutionSlider.setPaintTicks(false);

                        // startBtn.setText(btns[0]);

                    }
                }
                return;
            }
        });

        addWindowListener(new WindowAdapter()
        {
            /* The manipulations after user hitting 'close' and before 
            closing process starts.*/
            public void windowClosing(WindowEvent e)
            {
                // Destroy the py thread before closing the window
                pydestroy();
                // Delete the photo created from camera
                try{
                    new File(ssgWS + "SSGSHOTS_IMG.jpg").delete();
                    new File(ssgWS + "SSGSHOTS_IMG.py").delete();
                }catch(Exception er){}
                System.exit(0);
            }
        });

        // HELP INFORMATION - - - - - - - - - - - - - - - - - - -
        startBtn.setToolTipText(helpTips[0]);
        txtOutput.setToolTipText(helpTips[1]);
        optxtBtn.setToolTipText(helpTips[3]);
        stringInputField.setToolTipText(helpTips[2]);
        modeBox.setToolTipText(helpTips[4]);
        resetBtn.setToolTipText(helpTips[5]);
        wordComplexitySlider.setToolTipText(helpTips[6]);
        sliderLable.setToolTipText(helpTips[6]);
        resolutionSlider.setToolTipText(helpTips[8]);
        sliderLable2.setToolTipText(helpTips[8]);
        fontBox.setToolTipText(helpTips[7]);
        charInputField.setToolTipText(helpTips[9]);

        stringInputField.setDragEnabled(true);
        // OTHER SETTINGS - - - - - - - - - - - - - - - - - - - -
        txtOutput.setEditable(true);
        txtOutput.setFont(txtOutputFont);
        // Set auto wrap
        txtOutput.setLineWrap(true);
        txtOutput.setWrapStyleWord(true);
        // Set btn and char input not visible but in the content.
        charInputField.setVisible(false);
        fontLable.setVisible(false);
        
        fontBox.setVisible(false);
        // Set effect of sliders: having tick and space & size
        wordComplexitySlider.setMajorTickSpacing(1);
        wordComplexitySlider.setSnapToTicks(true);;
        wordComplexitySlider.setPaintTicks(true);
        wordComplexitySlider.setPreferredSize(new Dimension(130,30));
        resolutionSlider.setPreferredSize(new Dimension(130,30));
        areaScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(800, 800));
        areaScrollPane.setAutoscrolls(true);
        cuScaleChar = getCusScale();

        // ADD COMPONENTS - - - - - - - - - - - - - - - - - - - -
        pnlObj.add(inputLable);
        pnlObj.add(stringInputField);
        pnlObj.add(charInputField);
        pnlObj.add(fontLable);
        pnlObj.add(fontBox);
        pnlObj.add(startBtn);
        pnlObj.add(resetBtn);

        pnlObj.add(sliderLable2);
        pnlObj.add(resolutionSlider);
        pnlObj.add(sliderLable);
        pnlObj.add(wordComplexitySlider);
        pnlObj.add(reverseBtn);
        pnlObj.add(optxtBtn);
        pnlObj.add(modeBox);
        pnlObj.add(areaScrollPane);

        pack();
        setSize(800, 835);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Since we have already set it up
        add(pnlObj);
        setResizable(false);
        //setAlwaysOnTop(true);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e){}
    public static void main (String[] args) throws IOException
    {
        if (args.length != 0) {
            ;
        }

        // Create work space if needed
        File ssgWSObj = new File(ssgWS);
        if(!ssgWSObj.exists())
            ssgWSObj.mkdir();

        // Show UI
        info("Welcome to CharGrapher "+ ver);
        info("Console will also get updates and warnings");
        System.out.println();

        new CharGrapher();
        // Receive input from console
        do {
            // Print interface
            info("Please input image path below");
            System.out.print(">");
            input = scr.nextLine();
        } while (
            // This expression means if not exist, redo.
            !(new File(input).exists())
            );
        // Close buffer
        scr.close();

        photoToGraph(input);
    }


    static void photoToGraph(String input) throws IOException
    {
        File file = new File(input);
        if(!file.exists()) {
            errinfo("Sorry, file you inputted does not exist");
            return;
        }
        // Check elligibility
        if (!checkIfPic(file)) return;

        // Treat it as image file and give image data to bufferedimage type img.
        CGImage cgimage = new CGImage(file);

        // Check if resolution oversized. If it's oversized, compress before continue.
        if (cgimage.resolution > resolutionSlider.getValue())
        {
            /* Calculate the percentile in which is hoped to compress before letting imgCompress
            method to do the work. It's a algorithm. */
            double x = (double) resolutionSlider.getValue() / cgimage.resolution;
            x = Math.sqrt(x);
            cgimage.compress( (int)(cgimage.height * x * 0.8), (int)(cgimage.width * x) );
        }

        /* This is a logarithm that output the specific charactor(char) from the scale. The 
        position of char is determined from the complexity slider or internally built variable. 
        Meanwhile, here implements a log that remove extra whitespace from the output.   */

        // Initialize some variables will be used in later
        boolean spaceTest = true;
        String speChar = "";
        // Read each pixel and get each RGB value, proceed each one seperately.
        for (int i = 0; i < cgimage.height; i++) {
            // Ignore the rule if cutup is turned down
            if(!isCutUpSpacePart) output("");
            if(!spaceTest) output("");

            for (int j = 0; j < cgimage.width; j++) {
                int rgb = cgimage.img.getRGB(j, i);
                // Convert each pixel into average gray value
                int scalePlace = getScaleChar(getGrayValue(Integer.toHexString(rgb)));
                speChar = cuScaleChar.split("")[scalePlace];
                // If it's not a space, then stop not inputting
                if (isCutUpSpacePart && spaceTest && !(speChar.equals(" "))) {
                    spaceTest = false;
                    txtOutput.setText("");
                    // Align the text
                    output(" ".repeat(j+1));
                }
                output(speChar);
            }
        }
        // Resume for the next time
        spaceTest = true;

        System.out.println();
    }

    static void charToGraph() 
    {
        // Seperate each line by \n and proceed them individually.
        if (charInputField.getText().length() == 0) {
            errinfo("The Word Field cound't be empty");
            return;
        }
        for (int i = 0; i < charInputField.getText().split("]").length; i++) {
            // Create a blank canvas
            Graphics2D graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();

            // Get the size of the font, help to create a new String
            charFont = new Font(fontBox.getSelectedItem().toString(), Font.PLAIN, resolutionSlider.getValue());
            FontMetrics metrics = graphics.getFontMetrics(charFont);
            int hgt = metrics.getHeight();
            int adv = metrics.stringWidth(charInputField.getText().split("]")[i]);

            // Create the canves with expected dimension
            CGImage canvas = new CGImage(new BufferedImage(adv+4, hgt, BufferedImage.TYPE_INT_RGB)); 
            graphics = canvas.img.createGraphics();

            // Fill with all white
            graphics.fillRect(0, 0, adv+4, hgt);
            graphics.setColor(Color.BLACK);

            // Set the font and output
            graphics.setFont(charFont);
            graphics.drawString(charInputField.getText().split("]")[i], 0, hgt-5); // Draw String on canvas

            // Get basic information of the BufferedImage
            output(canvas.img, canvas.height, canvas.width, true);
        }
    }

    static void photoToHex(String path) {
        ;
    }

// - - - - - - - - - - - - - - - - - - - - I M G   P R O C E S S - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - I M G   P R O C E S S - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - I M G   P R O C E S S - - - - - - - - - - - - - - - - -

    /**
     * Calculate and return the most accurate grayscale (by pixel).
     * @param argb
     * @return Integer
     */
    static int getGrayValue(String argb) {
        // Convert from hexadecimal to decimal and get RGB from the String.
        int r = Integer.parseInt(argb.substring(2,4), 16);
        int g = Integer.parseInt(argb.substring(4,6), 16);
        int b = Integer.parseInt(argb.substring(6,8), 16);
        // EVEN
        String average = Integer.toHexString((r + g + b) / 3);
        if (average.length() == 1) average = "0" + average; //format to 2 units.
        // Calculate average value for ARGB
        return Integer.parseInt(average, 16);
    }

    /**
     * Method will return the position+1 of array in integer 
     */
    static int getScaleChar(int grayValue) {
        // grayValue will vary from 0 to 255, which is from pure black to pure white.
        return Math.round((float)(grayValue / 3.24686));
    }

    /**
     * Check if the type of image (file) is accepted.
     * @param File
     * @return boolean
     */
    static boolean checkIfPic(File file) {
        try {
            if (ImageIO.read(file) == null)
                throw new Exception("No data!");
            return true;
        } catch(Exception e) {
            errinfo("Sorry, the file you inputted is not supported");
            return false;
        }
    }


// - - - - - - - - - - - - - - - - - - - - - O T H E R - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - O T H E R - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - O T H E R - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Output the content to GUI or Console.
     * @param bufferedImg
     * @param imgHeight
     * @param imgWidth
     * @param isOutputToConsole
     */
    static void output (
        BufferedImage bufferedImg, 
        int imgHeight, 
        int imgWidth, 
        boolean isOutputToConsole
        )
    {
        // Get customized scale (from slider)
        String str = "";
        for (int i = 0; i < imgHeight; i++) {
            str += "\n";
            for (int j = 0; j < imgWidth; j++) {
                int scalePlace = CharGrapher.getScaleChar(CharGrapher.getGrayValue(Integer.toHexString(bufferedImg.getRGB(j, i))));
                str += (CharGrapher.cuScaleChar.substring(scalePlace,scalePlace+1));                    
            }
        }
        // If its words -> chargraph mode
        if (modeBox.getSelectedIndex() == 1) {
            txtOutput.append(str);
        } else {
            txtOutput.setText(str);
        }
        if (isOutputToConsole) System.out.print(str);
    }

    static void output(String str)
    {
        if(str.length() == 0)
        {
            // Console Output - Uncontrollable
            System.out.println();
            // GUI output - Controllable (set it in the field part)
            txtOutput.append("\n");
            return;
        }
        System.out.print(str);
        txtOutput.append(str);
        return;
    }

    static String getCusScale()
    {
        try{
            return scales[wordComplexitySlider.getValue()-1];
        } catch(ArrayIndexOutOfBoundsException e)
        {
            // If there's an error in getting the value, then use 0 position in array to replace
            return scales[0];
        }
    }

    static boolean outputToTxt()
    {
        String str = txtOutput.getText();
        // If it's camera mode and if it's not pausing
        if(str.length()==0)
        {
            if(modeBox.getSelectedItem().toString().equals(modes[2]) && 
            !startBtn.getText().equals(btns[4]))
            {
                while(str.length()==0)
                    str = txtOutput.getText();
            } else {
                return false;
            }
        }

        String opFileName = fileName + "_PLAIN_STRING_CONTENT_FROM_SSG"
        + (int) (Math.random() * 2000000 + 1000000) + ".txt";
        // Directly call the method to output.
        try {
            var file = new File(ssgWS + opFileName);
            bw = new BufferedWriter(new FileWriter(ssgWS + opFileName));
            /* Avoid when user pressed the "capture", the software was 
            refreshing its field */
            bw.write(str);
            bw.close();
            Desktop.getDesktop().open(file);
            if (!file.exists()) return false;
            return true;
        } catch (IOException er) {return false;}
    }

    static void camModeProcess()
    {                        
        if(!pyLaunch()) return;
        Thread snapshotpy = new Thread(new Snapshotpy());
        snapshotpy.start();
    }

    static void pydestroy()
    {
        pyhasdestroid = true;
        try{
            pylaunch.destroyForcibly();
        }catch(Exception e){
            pyhasdestroid = false;
        }
    }

    static boolean buildPy()
    {
        try
        {
            var out = new BufferedWriter(new FileWriter(new File(ssgWS + "SSshoter.py")));
            String prg = "from cv2 import *\n" +
            "import time\n"+
            "os.chdir(\"" + ssgWS + "\")\n"+
            "while True:\n"+
            /* Sleep(seconds) 0.1 is recommended. If the speed of read and write of your disk
               is obnormally low, you could adjust this value higher but you will experience 
               long delays.
               It means the delay in which python takes photo. */
            "    time.sleep("+ 0.3 +")\n"+ 
            "    cam = VideoCapture(0)\n"+
            "    rep, img = cam.read()\n"+
            "    imwrite(\"SSGSHOTS_IMG.jpg\",img)\n";
            out.write(prg);
            out.close();
        } catch (Exception e) {
            errinfo("Sorry, unable to output file. " + e.toString());
            return false;
        }
        return true;
    }

    static boolean pyLaunch()
    {
        try {
            pylaunch = Runtime.getRuntime().exec("python3 " + ssgWS + "SSshoter.py");
        }
        catch (Exception e) {
            errinfo("Sorry, unable to Launch python3. Please install the environment or check" +
            "if the python script is exist." + e.toString());
            return false;
        }
        pyhasdestroid = false;
        return true;
    }


// - - - - - - - - - - - - - - - - - - - - F O R   D I S P L A Y - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - F O R   D I S P L A Y - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - F O R   D I S P L A Y - - - - - - - - - - - - - - - - - -

    static void info(String info) {
        System.out.println("\033[0;32m" + "+INFO+ - " + info + "." + "\033[0m");
        return;
    }

    static void errinfo(String info) {
        System.out.println("\033[0;31m" + "+ERROR+ - " + info + "." + "\033[0m");
        JOptionPane.showMessageDialog(null, info + ".", "An Error Occurs", JOptionPane.ERROR_MESSAGE);
        return;
    }

    static void warninfo(String info) {
        System.out.println("\033[0;33m" + "+WARNING+ - " + info + "." + "\033[0m");
        return;
    }

// - - - - - - - - - - - - - - - - - - - N E S T E D   C L A S S - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - N E S T E D   C L A S S - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - N E S T E D   C L A S S - - - - - - - - - - - - - - - - - -

    /* Some of the picture processing methods are listed here. But main procedure was 
    stored in main method in nominated main class. */

    static class Snapshotpy implements Runnable 
    {
        BufferedImage bufferedImg;

        // To receive content that will be outputted
        String str = "";
        int imgWidth, imgHeight, imgResolution;
        double x = 0;
        String imgPath = CharGrapher.ssgWS + "SSGSHOTS_IMG.jpg";
        File imgfile = new File(imgPath);
        
        public void run()
        {
            // If python script task is alive, then continue to output. 
            while(!CharGrapher.pyhasdestroid)
            {
                try{
                    /* Thread.leep(MS) 100ms = .1s This means the delay in which the
                    software read image from the disk. If the value is lower, it may
                    result in exceeding disk consumption. Vice versa. */
                    Thread.sleep(1);
                    picproc();
                }catch(Exception e){}
            }
            return;
        }

        void picproc() {
            CGImage pic = new CGImage(imgfile);
            if(pic.resolution > CharGrapher.maxResolution)
            {
                // Get the ratio to compress
                x = CharGrapher.maxResolution / pic.resolution;
                x = Math.sqrt(x);
                // The 0.8 and 1.1 is ratio that adjust the output to suit the font.
                pic.compress((int) (pic.height * x * 0.8), (int) (pic.width * x * 1.1));
            }
            // Output the image. 
            CharGrapher.output(pic.img, pic.height, pic.width, false);
        }
    }

    static class CGImage {
        int width,height,resolution;
        BufferedImage img;
        
        public CGImage(File path) {
            // Since the path here is from the trusted source(after processed and checked), exception is no need to be handled.
            try {
                img = ImageIO.read(path);
            } catch(Exception e) {}
            width = img.getWidth();
            height = img.getHeight();
            resolution = width * height;
        }

        public CGImage(BufferedImage img) {
            this.img = img;
            width = img.getWidth();
            height = img.getHeight();
            resolution = width * height;
        }

        BufferedImage getImage() {
            return img;
        }

        void compress(int height, int width) {
            try {
                Image trimSize = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.drawImage(trimSize, 0, 0, Color.WHITE, null);
                g2d.dispose();
                img = resized;
            } catch(Exception e) {
                errinfo("Sorry: " + e + " was raised. Image was unable to be proceeded");
                e.printStackTrace();
            }
            this.height = height;
            this.width = width;
        }
    }
}
