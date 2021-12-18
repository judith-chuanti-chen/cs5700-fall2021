package transportlayer;

import common.Util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static common.Config.*;

public class Packet {
    private short type;
    private short seqNum;
    private short checksum;
    private byte[] payload;

    private Packet(short type, short seqNum) {
        this.type = type;
        this.seqNum = seqNum;
        this.checksum = calculateChecksum(this.toByteArrayAckNoChecksum());
    }

    private Packet(short type, short seqNum, byte[] payload) {
        this.type = type;
        this.seqNum = seqNum;
        this.payload = payload;
        this.checksum = calculateChecksum(this.toByteArrayDataNoChecksum());
    }

    public Packet(short type, short seqNum, short checksum, byte[] payload) {
        this.type = type;
        this.seqNum = seqNum;
        this.checksum = checksum;
        this.payload = payload;
    }

    public static Packet createPacket(short type, short seqNum, byte[] payload) {
        return new Packet(type, seqNum, payload);
    }

    public static Packet createAckPacket(short type, short seqNum) {
        return new Packet(type, seqNum);
    }

    public static Packet parsePacket(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        short type = bb.getShort();
        short seqNum = bb.getShort();
        short checksum = bb.getShort();
        int n = data.length - TYPE_LENGTH - SEQ_NUM_LENGTH - CHECKSUM_LENGTH;
        byte[] payload = new byte[n];
        bb.get(payload); // equivalent to bb.get(this.payload, 0, n);
        return new Packet(type, seqNum, checksum, payload);
    }
    public byte[] toByteArray() throws IllegalMsgTypeException {
        switch(type) {
            case MSG_TYPE_ACK:
                return toByteArrayAck();
            case MSG_TYPE_DATA:
                return toByteArrayData();
            default:
                throw new IllegalMsgTypeException("MSG_TYPE: " + type + " is not valid");
        }
    }
    public byte[] toByteArrayAck() {
        return ByteBuffer.allocate(TYPE_LENGTH + SEQ_NUM_LENGTH + CHECKSUM_LENGTH)
                .putShort(type)
                .putShort(seqNum)
                .putShort(checksum)
                .array();
    }

    public byte[] toByteArrayAckNoChecksum() {
        return ByteBuffer.allocate(TYPE_LENGTH + SEQ_NUM_LENGTH)
                .putShort(type)
                .putShort(seqNum)
                .array();
    }

    public byte[] toByteArrayData() {
        return ByteBuffer.allocate(TYPE_LENGTH + SEQ_NUM_LENGTH + CHECKSUM_LENGTH + payload.length)
                .putShort(type)
                .putShort(seqNum)
                .putShort(checksum)
                .put(payload)
                .array();
    }

    public byte[] toByteArrayDataNoChecksum() {
        return ByteBuffer.allocate(TYPE_LENGTH + SEQ_NUM_LENGTH + payload.length)
                .putShort(type)
                .putShort(seqNum)
                .put(payload)
                .array();
    }

    public boolean isCorrupted() {
        if (type == MSG_TYPE_ACK) {
            return calculateChecksum(this.toByteArrayAckNoChecksum()) != checksum;
        } else if (type == MSG_TYPE_DATA) {
            return  calculateChecksum(this.toByteArrayDataNoChecksum()) != checksum;
        }
        return true;
    }

//    private short calculateChecksum() {
//        long checksum = calculateDataSum();
//        // flip bits
//        checksum = ~checksum;
//        checksum = checksum & 0XFFFF;
//        return (short) checksum;
//    }

    private static short calculateChecksum(byte[] buffer) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(buffer);
        return ByteBuffer.wrap(Arrays.copyOfRange(md.digest(), 0, 2)).getShort();
    }

    private short calculateSum(){
        long sum = calculateDataSum();
        sum += checksum & 0XFFFF;
        sum = addCarry(sum);
        return (short) sum;
    }

    // Helper function to sum type, seqNum & payload
    private long calculateDataSum(){
        long sum = type & 0xFFFF + seqNum & 0xFFFF; // promoted to long (preserve the overflow)
        sum = addCarry(sum);
//        System.out.println("after add carry = " + Long.toBinaryString(sum));
        // If it is an ACK
        if (type == MSG_TYPE_ACK) {
            return sum;
        }
        int n = payload.length, i = 0;
        sum &= 0xFFFF;
        while (i < n - 1) {
            // add two 8-bit bytes which becomes 16-bit
            sum += (((payload[i++] << 8) & 0xFF00) | ((payload[i++]) & 0xFF));
            // add carry bit to checksum
            sum = addCarry(sum);

        }
        // add the remaining byte if any
        if (i < n) {
            sum += (payload[i] << 8) & 0xFF00;
            // add carry bit to checksum
            sum = addCarry(sum);
        }
        return sum;
    }

    private long addCarry(long checksum) {
        if ((checksum & 0xFFFF0000) != 0) {
            checksum &= 0xFFFF;
            checksum += 1;
        }
        return checksum;
    }


    public short getType() {
        return type;
    }

    public short getSeqNum() {
        return seqNum;
    }

    public short getChecksum() {
        return checksum;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type=" + type +
                ", seqNum=" + (seqNum & 0xFFFF) +
                ", checksum=" + (checksum & 0xFFFF) +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }

//    public static void main(String[] args) throws Exception {
//        short type = MSG_TYPE_DATA;
//        for(int i = 0; i < 50; i++){
//            short seqNum = (short) i;
//            byte[] payload = String.format("MSG: %s", i).getBytes();
//            Packet p = Packet.createPacket(type, seqNum, payload);
//            byte[] corrupted = Util.randomBitError(p.toByteArray().clone());
//            Packet newP = Packet.parsePacket(corrupted);
//            if (!newP.isCorrupted()){
//                System.out.println(String.format("original seqNum=%s, bytes=%s", seqNum, new BigInteger(p.toByteArray()).toString(2)));
//                System.out.println(String.format("corrupted seqNum=%s, bytes=%s", seqNum, new BigInteger(corrupted).toString(2)));
//                System.out.println("calculated checksum=" + (p.calculateChecksum() & 0XFFFF));
//                System.out.println("original type=" + Integer.toBinaryString(p.getType() & 0XFFFF));
//                System.out.println("received type=" + Integer.toBinaryString(newP.getType() & 0XFFFF));
//                System.out.println("original seqNum=" + Integer.toBinaryString((p.getSeqNum() & 0XFFFF)));
//                System.out.println("received seqNum=" + Integer.toBinaryString((newP.getSeqNum() & 0XFFFF)));
//                System.out.println("original payload=" + newP.getPayload().toString());
//                System.out.println("received payload=" + newP.getPayload().toString());
//                System.out.println("received checksum=" + (newP.getChecksum() & 0XFFFF));
//                System.out.println("received sum=" + (newP.calculateSum() & 0XFFFF));
//                System.out.println("isCorrupted=" + newP.isCorrupted());
//                System.out.println();
//            }
//
//        }
//    }
}
