import com.sun.xml.internal.bind.v2.util.ByteArrayOutputStreamEx;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

public class Server {
    //private int keySize;
    private byte[] key;
    private int[] prekeys = new int[140];
    private static int openKey;
    private static int r;

    public int blockSize() {
        return 16;
    }

    public int keySize() {
        return 32;
    }

    public void setKey(byte[] key) {
        if (key.length != keySize()) {
            this.key = new byte[keySize()];
            for( int i = 0; i < key.length; i++ ) {
                this.key[i] = key[i];
            }
            for( int i = key.length; i < keySize(); i++ ) {
                if( i == key.length ) {
                    //Start of padding!
                    this.key[i] = (byte)0x80;
                }else {
                    this.key[i] = (byte)0x00;
                }
            }
        }else {
            this.key = key;
        }

        for(int i = 0; i < 8; i++) {
            prekeys[i] = this.key[4 * i];
            prekeys[i] = (prekeys[i] << 8) | this.key[4 * i + 1];
            prekeys[i] = (prekeys[i] << 8) | this.key[4 * i + 2];
            prekeys[i] = (prekeys[i] << 8) | this.key[4 * i + 3];
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

    public void encrypt(byte[] text) {
        byte[] data = initPermutation(text);
        byte[] temp = new byte[] {
                data[12], data[13], data[14], data[15],
                data[8], data[9], data[10], data[11],
                data[4], data[5], data[6], data[7],
                data[0], data[1], data[2], data[3],
        };
        data = temp;
        byte[] roundKey = new byte[16];
        //32 rounds
        for(int i = 0; i < 32; i++){
            roundKey = getRoundKey(i);
            for(int n = 0; n < 16; n++){
                data[n] = (byte) (data[n] ^ roundKey[n]);
            }
            data = sBox(data, i);

            if(i == 31){
                roundKey = getRoundKey(32);
                for(int n = 0; n < 16; n++){
                    data[n] = (byte) (data[n] ^ roundKey[n]);
                }
            }
            else{
                data = linearTransform(data);
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

    private byte[] getRoundKey(int round) {
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

    private byte[] initPermutation(byte[] data) {
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

    private byte[] finalPermutation(byte[] data) {
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

    private byte[] sBox(byte[] data, int round) {
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

    private byte[] linearTransform(byte[] data){
        data = finalPermutation(data);
        byte[] output = new byte[blockSize()];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int x0 =  buffer.getInt();
        int x1 =  buffer.getInt();
        int x2 =  buffer.getInt();
        int x3 =  buffer.getInt();
        x0 = ((x0 << 13) | (x0 >>> (32 - 13)));
        x2 = ((x2 << 3) | (x2 >>> (32 - 3)));
        x1 = x1 ^ x0 ^ x2;
        x3 = x3 ^ x2 ^ (x0 << 3);
        x1 = (x1 << 1) | (x1 >>> (32 - 1));
        x3 = (x3 << 7) | (x3 >>> (32 - 7));
        x0 = x0 ^ x1 ^ x3;
        x2 = x2 ^ x3 ^ (x1 << 7);
        x0 = (x0 << 5) | (x0 >>> (32-5));
        x2 = (x2 << 22) | (x2 >>> (32-22));
        buffer.clear();
        buffer.putInt(x0);
        buffer.putInt(x1);
        buffer.putInt(x2);
        buffer.putInt(x3);

        output = buffer.array();
        output = initPermutation(output);

        return output;
    }

    public static byte[] fileToByte(String filePath) throws IOException{
        byte[] data = new byte[1];

        File input = new File(filePath);
        if (input.exists()){
            FileInputStream in = new FileInputStream(input);
            ByteArrayOutputStream out = new ByteArrayOutputStreamEx(2048);
            byte[] byteStr = new byte[1024];
            int nRead = 0;
            while((nRead = in.read(byteStr)) != -1){
                out.write(byteStr, 0, nRead);
                out.flush();
            }

            out.close();
            in.close();
            data = out.toByteArray();
        }
        return data;
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

    public static void main(String[] args) throws InterruptedException{
        try (ServerSocket server = new ServerSocket(3345)){
            Socket client = server.accept();
            System.out.println("Connection accepted!");

            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            System.out.println("DataOutputStream created");

            DataInputStream in = new DataInputStream(client.getInputStream());
            System.out.println("DataInputStream created");

            while (!client.isClosed()){
                System.out.println("Server is reading from channel");

                String entry = in.readUTF();
                System.out.println("Read from client " + entry);

                if (entry.equalsIgnoreCase("quit")){
                    System.out.println("Client is finished");
                    out.writeUTF("Server replies " + entry + " ok");
                    out.flush();
                    Thread.sleep(3000);
                    break;
                }

                openKey = Integer.parseInt(in.readUTF());
                r = Integer.parseInt(in.readUTF());

                System.out.println("Recieve open key " + openKey + "\n");

                System.out.println("Server tries to write to channel");

                byte [] fileData = fileToByte("Server/src/" + entry);
                //byte[] keyFromClient = fileToByte("D:/Учёба/4 курс/КБРС/Serpent/src/key.txt");
                byte[] keyFromClient = new byte[32];
                Random rand = new Random();
                for (int i = 0; i < 32; i++){
                    keyFromClient[i] = (byte)(Math.abs(rand.nextInt() % 200) + 1);
                }

                Server serv = new Server();

                //encrypting
                int numberOfBlocks = fileData.length % 16 == 0 ? fileData.length / 16 : fileData.length / 16 + 1;

                if (fileData.length % 16 != 0){
                    byte[] newFileData = new byte[numberOfBlocks * 16];
                    for (int i = 0; i < fileData.length; i++){
                        newFileData[i] = fileData[i];
                    }

                    for (int i = fileData.length; i < numberOfBlocks * 16; i++){
                        newFileData[i] = 0;
                    }
                    fileData = newFileData;
                }

                serv.setKey(keyFromClient);
                out.writeUTF(Integer.toString(numberOfBlocks * 16));
                out.flush();

                byte[] initVector = new byte[16];
                initVector = fileToByte("initVector.txt");
                for (int i = 0; i < 16; i++){
                    out.writeUTF(Byte.toString(initVector[i]));
                }

                for (int i = 0; i < serv.key.length; i++) {
                    int part = fastExp(serv.key[i], openKey, r);
                    out.writeUTF(Integer.toString(part));
                    out.flush();
                }

                byte[] prevBlock = new byte[16];
                for (int j = 0; j < numberOfBlocks; j++) {
                    byte[] block = new byte[16];
                    for (int i = j * 16, k = 0; i < (j + 1) * 16; i++, k++) {
                        block[k] = fileData[i];
                    }

                    if (j == 0){
                        for (int i = 0; i < 16; i++){
                            block[i] ^= initVector[i];
                        }
                    }
                    else{
                        for (int i = 0; i < 16; i++){
                            block[i] ^= prevBlock[i];
                        }
                    }

                    serv.encrypt(block);

                    for (int i = 0; i < block.length; i++) {
                        out.writeUTF(Byte.toString(block[i]));
                        out.flush();
                    }
                    prevBlock = block;
                }
                System.out.println("Server wrote message to client.");
            }

            System.out.println("Client disconnected");
            System.out.println("Closing connections & channels.");

            in.close();
            out.close();

            client.close();

            System.out.println("Closing connections & channels - DONE.");
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
