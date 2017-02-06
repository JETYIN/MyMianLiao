package com.tjut.mianliao.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.Rect;

public class NinePatchChunk {

    private int[] mDivX;
    private int[] mDivY;
    private int[] mColor;
    private Rect mPaddings;

    public Rect getPaddings() {
        return mPaddings;
    }

    public static NinePatchChunk deserialize(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());

        if (byteBuffer.get() == 0) {
            return null;
        }

        NinePatchChunk chunk = new NinePatchChunk();
        chunk.mDivX = new int[byteBuffer.get()];
        chunk.mDivY = new int[byteBuffer.get()];
        chunk.mColor = new int[byteBuffer.get()];

        if (!checkDivCount(chunk.mDivX.length) || !checkDivCount(chunk.mDivY.length)) {
            return null;
        }

        // skip 8 bytes
        byteBuffer.getInt();
        byteBuffer.getInt();

        int left = byteBuffer.getInt();
        int right = byteBuffer.getInt();
        int top = byteBuffer.getInt();
        int bottom = byteBuffer.getInt();
        chunk.mPaddings = new Rect(left, top, right, bottom);

        // skip 4 bytes
        byteBuffer.getInt();

        readIntArray(chunk.mDivX, byteBuffer);
        readIntArray(chunk.mDivY, byteBuffer);
        readIntArray(chunk.mColor, byteBuffer);

        return chunk;
    }

    private static void readIntArray(int[] data, ByteBuffer buffer) {
        for (int i = 0, n = data.length; i < n; ++i) {
            data[i] = buffer.getInt();
        }
    }

    private static boolean checkDivCount(int length) {
        return length != 0 && (length & 0x01) == 0;
    }
}