import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Scanner;

public class Client extends JFrame{
    private static byte[] key;
    private static int[] prekeys = new int[140];
    private static int privateKey;
    private static int openKey;
    private static int r;

    private JTextField editFileName = new JTextField(20);
    private JTextField editPrivateKey = new JTextField(20);
    private JTextField editOpenKey = new JTextField(20);

    private static byte[] ipTable = new byte[] {
            0, 32, 64,  96,  1, 33, 65,  97,  2, 34, 66,  98,  3, 35, 67,  99,
            4, 36, 68, 100,  5, 37, 69, 101,  6, 38, 70, 102,  7, 39, 71, 103,
            8, 40, 72, 104,  9, 41, 73, 105, 10, 42, 74, 106, 11, 43, 75, 107,
            12, 44, 76, 108, 13, 45, 77, 109, 14, 46, 78, 110, 15, 47, 79, 111,
            16, 48, 80, 112, 17, 49, 81, 113, 18, 50, 82, 114, 19, 51, 83, 115,
            20, 52, 84, 116, 21, 53, 85, 117, 22, 54, 86, 118, 23, 55, 87, 119,
            24, 56, 88, 120, 25, 57, 89, 121, 26, 58, 90, 122, 27, 59, 91, 123,
            28, 60, 92, 124, 29, 61, 93, 125, 30, 62, 94, 126, 31, 63, 95, 127
    };

    private static byte[] fpTable = new byte[] {
            0,  4,  8, 12, 16, 20, 24, 28, 32,  36,  40,  44,  48,  52,  56,  60,
            64, 68, 72, 76, 80, 84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 124,
            1,  5,  9, 13, 17, 21, 25, 29, 33,  37,  41,  45,  49,  53,  57,  61,
            65, 69, 73, 77, 81, 85, 89, 93, 97, 101, 105, 109, 113, 117, 121, 125,
            2,  6, 10, 14, 18, 22, 26, 30, 34,  38,  42,  46,  50,  54,  58,  62,
            66, 70, 74, 78, 82, 86, 90, 94, 98, 102, 106, 110, 114, 118, 122, 126,
            3,  7, 11, 15, 19, 23, 27, 31, 35,  39,  43,  47,  51,  55,  59,  63,
            67, 71, 75, 79, 83, 87, 91, 95, 99, 103, 107, 111, 115, 119, 123, 127
    };

    private static byte[] s0 = new byte[]
            {3,8,15,1,10,6,5,11,14,13,4,2,7,0,9,12};
    private static byte[] s1 = new byte[]
            {15,12,2,7,9,0,5,10,1,11,14,8,6,13,3,4};
    private static byte[] s2 = new byte[]
            {8,6,7,9,3,12,10,15,13,1,14,4,0,11,5,2};
    private static byte[] s3 = new byte[]
            {0,15,11,8,12,9,6,3,13,1,2,4,10,7,5,14};
    private static byte[] s4 = new byte[]
            {1,15,8,3,12,0,11,6,2,5,4,10,9,14,7,13};
    private static byte[] s5 = new byte[]
            {15,5,2,11,4,10,9,12,0,3,14,8,13,6,7,1};
    private static byte[] s6 = new byte[]
            {7,2,12,5,8,4,6,11,14,9,1,15,13,3,10,0};
    private static byte[] s7 = new byte[]
            {1,13,15,0,14,8,2,11,7,4,12,10,9,3,5,6};
    private static byte[][] sBoxes = new byte[][]
            {s0,s1,s2,s3,s4,s5,s6,s7};

    private static byte[] is0 = new byte[]
            {13,3,11,0,10,6,5,12,1,14,4,7,15,9,8,2};
    private static byte[] is1 = new byte[]
            {5,8,2,14,15,6,12,3,11,4,7,9,1,13,10,0};
    private static byte[] is2 = new byte[]
            {12,9,15,4,11,14,1,2,0,3,6,13,5,8,10,7};
    private static byte[] is3 = new byte[]
            {0,9,10,7,11,14,6,13,3,5,12,2,4,8,15,1};
    private static byte[] is4 = new byte[]
            {5,0,8,3,10,9,7,14,2,12,11,6,4,15,13,1};
    private static byte[] is5 = new byte[]
            {8,15,2,9,4,1,13,14,11,6,5,3,7,12,10,0};
    private static byte[] is6 = new byte[]
            {15,10,1,13,5,3,6,0,4,9,14,7,2,12,8,11};
    private static byte[] is7 = new byte[]
            {3,0,6,13,9,14,15,8,5,12,11,7,10,1,4,2};
    private static byte[][] isBoxes = new byte[][]
            {is0,is1,is2,is3,is4,is5,is6,is7};

    private static DataOutputStream oos;
    private static DataInputStream ois;
    private static Socket socket;

    public Client(){
        Container c = getContentPane();
        c.setLayout(new GridLayout(0, 1));

        JPanel buttonPanel = new JPanel((new FlowLayout()));
        JButton startConnection = new JButton("Соединение с сервером");
        startConnection.addActionListener(ev->{
            try{
                socket = new Socket("localhost", 3345);
                Scanner sc = new Scanner(new InputStreamReader(System.in));
                oos = new DataOutputStream(socket.getOutputStream());
                ois = new DataInputStream(socket.getInputStream());

                System.out.println("Client is ready");
                System.out.println();

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        buttonPanel.add(startConnection);
        JButton endConnection = new JButton("Завершить соединение");
        endConnection.addActionListener(e->{
            try {
                System.out.println("Client kill connections");
                Thread.sleep(2000);

                oos.writeUTF("quit");
                oos.flush();
                //ois.close();
                oos.close();
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
            catch (InterruptedException ex){
                ex.printStackTrace();
            }
        });
        buttonPanel.add(endConnection);
        JButton keyButton = new JButton("Сгенерировать ключ");
        keyButton.addActionListener(e->{
            Random rand = new Random();
            int p = Math.abs(rand.nextInt()) % 40 + 30;
            int q = Math.abs(rand.nextInt()) % 40 + 35;
            int pSimple = findSimple(p);
            int qSimple = findSimple(q);

            while (pSimple == qSimple){
                q = Math.abs(rand.nextInt()) % 80 + 35;
                qSimple = findSimple(q);
            }

            r = pSimple * qSimple;
            int phi = (pSimple - 1) * (qSimple - 1);

            int keyOp, d = 0;
            while (d != 1 || privateKey <= 0 || privateKey == openKey){
                keyOp = Math.abs(rand.nextInt()) % 100 + 100;
                if (keyOp < phi) {
                    d = gcd(phi, keyOp);
                }

                openKey = keyOp;

                privateKey = euclid(phi, openKey);
            }

            System.out.println("p = " + pSimple + " q = " + qSimple);
            editOpenKey.setText(Integer.toString(openKey));
            editPrivateKey.setText(Integer.toString(privateKey));
        });
        buttonPanel.add(keyButton);
        c.add(buttonPanel);

        JPanel xPanel = new JPanel();
        xPanel.setLayout(new BoxLayout(xPanel, BoxLayout.Y_AXIS));
        JLabel xLabel = new JLabel("Открытый ключ");
        xLabel.setAlignmentX(CENTER_ALIGNMENT);
        xPanel.add(xLabel);
        c.add(xPanel);

        JPanel editPanel = new JPanel(new FlowLayout());
        editPanel.add(editOpenKey);
        editOpenKey.setEnabled(false);
        c.add(editPanel);

        JPanel yPanel = new JPanel();
        yPanel.setLayout(new BoxLayout(yPanel, BoxLayout.Y_AXIS));
        JLabel yLabel = new JLabel("Закрытый ключ");
        yLabel.setAlignmentX(CENTER_ALIGNMENT);
        yPanel.add(yLabel);
        c.add(yPanel);

        JPanel editPanelY = new JPanel(new FlowLayout());
        editPanelY.add(editPrivateKey);
        editPrivateKey.setEnabled(false);
        c.add(editPanelY);

        JPanel nPanel = new JPanel();
        nPanel.setLayout(new BoxLayout(nPanel, BoxLayout.Y_AXIS));
        JLabel nLabel = new JLabel("File name");
        nLabel.setAlignmentX(CENTER_ALIGNMENT);
        nPanel.add(nLabel);
        c.add(nPanel);

        JPanel editText = new JPanel((new FlowLayout()));
        editText.add(editFileName);
        c.add(editText);

        JPanel submitPanel = new JPanel(new FlowLayout());
        JButton submitButton = new JButton("Отправить серверу");
        submitButton.addActionListener(e->{
            try {
                if (!socket.isOutputShutdown()) {

                    System.out.println("Client start writing in channel...");
                    Thread.sleep(1000);
                    String clientCommand = editFileName.getText();

                    oos.writeUTF(clientCommand);
                    oos.flush();

                    oos.writeUTF(Integer.toString(openKey));
                    oos.flush();

                    oos.writeUTF(Integer.toString(r));
                    oos.flush();

                    System.out.println("Client sent message " + clientCommand + " to server.");
                    System.out.println("Open key = " + openKey + " Private key = " + privateKey);

                    Thread.sleep(1000);

                    System.out.println("Client sent message & start waiting for data from server...");
                    Thread.sleep(2000);
                    String in;

                    if ((in = ois.readUTF()) != null) {
                        System.out.println("reading...");
                        int nBytes = Integer.parseInt(in);
                        byte[] inputData = new byte[nBytes];
                        byte[] sessionKey = new byte[32];
                        byte[] initVector = new byte[16];

                        System.out.println(in);

                        for (int i = 0; i < 16; i++) {
                            initVector[i] = Byte.parseByte(ois.readUTF());
                        }

                        for (int i = 0; i < 32; i++) {
                            int part = Integer.parseInt(ois.readUTF());
                            sessionKey[i] = (byte) (fastExp(part, privateKey, r));
                        }

                        int numberOfBlocks = nBytes / 16;

                        Client client = new Client();

                        //decryption
                        client.setKey(sessionKey);

                        byte[] readBlock = new byte[16];
                        byte[] prevBlock = new byte[16];

                        for (int j = 0; j < numberOfBlocks; j++) {
                            byte[] block = new byte[16];
                            for (int i = 0; i < 16; i++) {
                                block[i] = Byte.parseByte(ois.readUTF());
                                readBlock[i] = block[i];
                            }

                            client.decrypt(block);

                            if (j == 0) {
                                for (int i = j * 16, k = 0; i < (j + 1) * 16; i++, k++) {
                                    inputData[i] = (byte) (block[k] ^ initVector[k]);
                                }
                            } else {
                                for (int i = j * 16, k = 0; i < (j + 1) * 16; i++, k++) {
                                    inputData[i] = (byte) (block[k] ^ prevBlock[k]);
                                }
                            }

                            for (int i = 0; i < 16; i++) {
                                prevBlock[i] = readBlock[i];
                            }
                        }
                        byteArrayToFile(inputData, "output.txt");
                        System.out.println("DONE");
                    }
                }
            }
            catch(InterruptedException ex){
                ex.printStackTrace();
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        });
        submitPanel.add(submitButton);
        c.add(submitPanel);

    }

    public static int blockSize() {
        return 16;
    }

    /**
     * Returns this block cipher's key size in bytes.
     *
     * @return  Key size.
     */
    public static int keySize() {
        return 32;
    }

    public static void setKey(byte[] key1) {
        if (key1.length != keySize()) {
            key = new byte[keySize()];
            for( int i = 0; i < key1.length; i++ ) {
                key[i] = key1[i];
            }
            for( int i = key1.length; i < keySize(); i++ ) {
                if( i == key1.length ) {
                    key[i] = (byte)0x80;
                }else {
                    key[i] = (byte)0x00;
                }
            }
        }else {
            key = key1;
        }

        for(int i = 0; i < 8; i++) {
            prekeys[i] = key[4 * i];
            prekeys[i] = (prekeys[i] << 8) | key[4 * i + 1];
            prekeys[i] = (prekeys[i] << 8) | key[4 * i + 2];
            prekeys[i] = (prekeys[i] << 8) | key[4 * i + 3];
        }
        for( int i = 8; i < prekeys.length; i++ ) {
            byte[] prnt;
            int phi = 0x9e3779b9;
            int tmp;
            tmp = prekeys[i-8] ^ prekeys[i-5] ^ prekeys[i-3] ^ prekeys[i-1] ^
                    i-8 ^ phi;
            prekeys[i] = (tmp << 11) | (tmp >>> (21));
            prnt = new byte[4];
            prnt[0] = (byte)(prekeys[i] >> 24);
            prnt[1] = (byte)(prekeys[i] >> 16);
            prnt[2] = (byte)(prekeys[i] >> 8);
            prnt[3] = (byte)(prekeys[i]);
        }
    }

    public static void decrypt(byte[] text) {
        byte[] temp = new byte[] {
                text[3], text[2], text[1], text[0],
                text[7], text[6], text[5], text[4],
                text[11], text[10], text[9], text[8],
                text[15], text[14], text[13], text[12],
        };
        byte[] data = initPermutation(temp);
        byte[] roundKey = getRoundKey(32);
        for(int n = 0; n < 16; n++){
            data[n] = (byte) (data[n] ^ roundKey[n]);
        }
        //32 rounds in reverse
        for(int i = 31; i >= 0; i--){
            if(i!=31){
                data = invLinearTransform(data);
            }
            data = sBoxInv(data, i);
            roundKey = getRoundKey(i);
            for(int n = 0; n < 16; n++){
                data[n] = (byte) (data[n] ^ roundKey[n]);
            }
        }
        data = finalPermutation(data);
        text[0] = data[3];
        text[1] = data[2];
        text[2] = data[1];
        text[3] = data[0];
        text[4] = data[7];
        text[5] = data[6];
        text[6] = data[5];
        text[7] = data[4];
        text[8] = data[11];
        text[9] = data[10];
        text[10] = data[9];
        text[11] = data[8];
        text[12] = data[15];
        text[13] = data[14];
        text[14] = data[13];
        text[15] = data[12];
    }

    private static byte[] initPermutation(byte[] data) {
        byte[] output = new byte[16];
        for (int i = 0;  i < 128; i++) {
            int bit = (data[(ipTable[i]) / 8] >>> ((ipTable[i]) % 8)) & 0x01;
            if ((bit & 0x01) == 1)
                output[15- (i/8)] |= 1 << (i % 8);
            else
                output[15 - (i/8)] &= ~(1 << (i % 8));
        }
        return output;
    }

    private static byte[] finalPermutation(byte[] data) {
        byte[] output = new byte[16];
        for (int i = 0;  i < 128; i++) {
            int bit = (data[15-fpTable[i] / 8] >>> (fpTable[i] % 8)) & 0x01;
            if ((bit & 0x01) == 1)
                output[(i/8)] |= 1 << (i % 8);
            else
                output[(i/8)] &= ~(1 << (i % 8));
        }
        return output;
    }

    private static byte[] getRoundKey(int round) {
        int k0 = prekeys[4*round+8];
        int k1 = prekeys[4*round+9];
        int k2 = prekeys[4*round+10];
        int k3 = prekeys[4*round+11];
        int box = (((3-round)%8)+8)%8;
        byte[] in = new byte[16];
        for (int j = 0; j < 32; j+=2) {
            in[j/2] = (byte) (((k0 >>> j) & 0x01)     |
                    ((k1 >>> j) & 0x01) << 1 |
                    ((k2 >>> j) & 0x01) << 2 |
                    ((k3 >>> j) & 0x01) << 3 |
                    ((k0 >>> j+1) & 0x01) << 4 |
                    ((k1 >>> j+1) & 0x01) << 5 |
                    ((k2 >>> j+1) & 0x01) << 6 |
                    ((k3 >>> j+1) & 0x01) << 7 );
        }
        byte[] out = sBox(in, box);
        byte[] key = new byte[16];
        for (int i = 3; i >= 0; i--) {
            for(int j = 0; j < 4; j++) {
                key[3-i] |= (out[i*4+j] & 0x01) << (j*2) | ((out[i*4+j] >>> 4) & 0x01) << (j*2+1) ;
                key[7-i] |= ((out[i*4+j] >>> 1) & 0x01) << (j*2) | ((out[i*4+j] >>> 5) & 0x01) << (j*2+1) ;
                key[11-i] |= ((out[i*4+j] >>> 2) & 0x01) << (j*2) | ((out[i*4+j] >>> 6) & 0x01) << (j*2+1) ;
                key[15-i] |= ((out[i*4+j] >>> 3) & 0x01) << (j*2) | ((out[i*4+j] >>> 7) & 0x01) << (j*2+1) ;
            }
        }
        return initPermutation(key);
    }

    private static byte[] sBox(byte[] data, int round) {
        byte[] toUse = sBoxes[round%8];
        byte[] output = new byte[blockSize()];
        for( int i = 0; i < blockSize(); i++ ) {
            int curr = data[i]&0xFF;
            byte low4 = (byte)(curr>>>4);
            byte high4 = (byte)(curr&0x0F);
            output[i] = (byte) ((toUse[low4]<<4) ^ (toUse[high4]));
        }
        return output;
    }

    private static byte[] sBoxInv(byte[] data, int round) {
        byte[] toUse = isBoxes[round%8];
        byte[] output = new byte[blockSize()];
        for( int i = 0; i < blockSize(); i++ ) {
            int curr = data[i]&0xFF;
            byte low4 = (byte)(curr>>>4);
            byte high4 = (byte)(curr&0x0F);
            output[i] = (byte) ((toUse[low4]<<4) ^ (toUse[high4]));
        }
        return output;
    }

    private static byte[] invLinearTransform(byte[] data){
        data = finalPermutation(data);
        byte[] output = new byte[blockSize()];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int x0 =  buffer.getInt();
        int x1 =  buffer.getInt();
        int x2 =  buffer.getInt();
        int x3 =  buffer.getInt();

        x2 = (x2 >>> 22) | (x2 << (32-22));
        x0 = (x0 >>> 5) | (x0 << (32-5));
        x2 = x2 ^ x3 ^ (x1 << 7);
        x0 = x0 ^ x1 ^ x3;
        x3 = (x3 >>> 7) | (x3 << (32-7));
        x1 = (x1 >>> 1) | (x1 << (32-1));
        x3 = x3 ^ x2 ^ (x0 << 3);
        x1 = x1 ^ x0 ^ x2;
        x2 = (x2 >>> 3) | (x2 << (32-3));
        x0 = (x0 >>> 13) | (x0 << (32-13));

        buffer.clear();
        buffer.putInt(x0);
        buffer.putInt(x1);
        buffer.putInt(x2);
        buffer.putInt(x3);

        output = buffer.array();
        output = initPermutation(output);

        return output;
    }

    public static void byteArrayToFile(byte[] bytes, String filePath) throws IOException {
        String textStr = "";
        InputStream in = new ByteArrayInputStream(bytes);
        File destFile = new File(filePath);
        OutputStream out = new FileOutputStream(destFile);
        byte[] cache = new byte[1024];
        int nRead = 0;
        while ((nRead = in.read(cache)) != -1) {
            out.write(cache, 0, nRead);
            out.flush();
            for (int i = 0; i < cache.length; i++)
                textStr += Byte.toString(cache[i]);
        }
        out.close();
        in.close();

    }

    public static int findSimple(int n){
        boolean[] isSimple = new boolean[n + 1];
        isSimple[0] = isSimple[1] = false;

        for (int i = 2; i < n + 1; i++){
            isSimple[i] = true;
        }

        for (int i = 2; i < n + 1; i++){
            if (isSimple[i]){
                if (Math.pow(i, 2) <= n){
                    for (int j = (int)Math.pow(i, 2); j <= n; j += i){
                        isSimple[j] = false;
                    }
                }
                else
                    break;
            }
        }

        for (int i = n; i >= 0; i--){
            if (isSimple[i])
                return i;
        }

        return 2;
    }

    public static int gcd(int a, int b){
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }

    public static int euclid(int a, int b){
        int x0 = 1, x1 = 0, y0 = 0, y1 = 1;
        while(b > 1){
            int q = a / b;
            int d = a % b;
            int x2 = x0 - q * x1;
            int y2 = y0 - q * y1;
            a = b;
            b = d;
            x0 = x1;
            x1 = x2;
            y0 = y1;
            y1 = y2;
        }
        return y1;
    }

    public static int fastExp(int a, int z, int n){
        int x = 1;
        int a1 = a;
        while(z != 0){
            while (z % 2 == 0){
                z = z / 2;
                a1 = (a1 * a1) % n;
            }
            z--;
            x = (x * a1) %n;
        }
        return x;
    }

    public static void main(String[] args) throws InterruptedException {
        Client frame = new Client();
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(new Dimension(600, 600));
        frame.setVisible(true);
        frame.setResizable(false);
    }
}
