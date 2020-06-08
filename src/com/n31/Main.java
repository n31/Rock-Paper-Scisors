package com.n31;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import java.util.Random;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;



public class Main {

    static public Random num = new Random(System.currentTimeMillis());

    public static void main(String[] args) throws InvalidKeyException {

        if (args.length < 3 || args.length % 2 == 0 || !uniqueCheck(args)) {
            System.out.print("Error: invalid arguments ");
            return;
        }

        int playerMove = 0, compMove = 0;
        compMove = num.nextInt(args.length);

        String key = "";
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("HMACSHA256");
            SecureRandom secureRandom = new SecureRandom();
            int keyBitSize = 170; // 256 bit
            kgen.init(keyBitSize, secureRandom);
            SecretKey skey = kgen.generateKey();
            key = keyToString(skey);
            byte[] hmacSha256 = calcHmacSha256(key.getBytes("UTF-8"), args[compMove].getBytes("UTF-8"));
            System.out.println(String.format("HMAC: %032x", new BigInteger(1, hmacSha256)));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        do {
            for (int i = 0; i < args.length; i++) {
                System.out.println((i + 1) + " - " + args[i]);
            }
            System.out.print("0 - exit\nEnter your move: ");
            Scanner in = new Scanner(System.in);
            playerMove = in.nextInt();
        } while (playerMove < 0 || playerMove > args.length);
        if (playerMove == 0) return;
        playerMove--;
        System.out.println("Your move: " + args[playerMove]);
        System.out.println("Computer move: " + args[compMove]);
        if (playerMove == compMove) System.out.println("No winner!");
        else {
            if (check(args, playerMove, compMove)) System.out.println("You win!");
            else System.out.println("You lose!");
        }

        System.out.println("HMAC key:\n" + key);
    }

    private static boolean check(String[] args, int p, int c) {
        int middle = args.length / 2;
        int difference = middle - p;
        if (difference > 0) {
            for (int i = 0; i < difference; i++) {
                p++;
                c++;
                if (c == args.length) c = 0;
            }
        }
        else {
            difference *= -1;
            for (int i = 0; i < difference; i++) {
                p--;
                c--;
                if (c == -1) c = args.length;
            }
        }
        return c < p;
    }

    private static byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
        byte[] hmacSha256 = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha256", e);
        }
        return hmacSha256;
    }

    private static String keyToString(SecretKey secretKey) {
        byte encoded[] = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    private static boolean uniqueCheck(String[] arr) {
        for(int i = 0; i < arr.length; i++) {
            for(int j = i + 1; j < arr.length; j++) {
                if(arr[i].equals(arr[j]))
                    return false;
            }
        }
        return true;
    }
}
