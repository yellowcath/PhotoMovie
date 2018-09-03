package com.hw.photomovie.opengl;

import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

/**
 * Open GL ES 1.1 implementation for generating and destroying texture IDs and
 * buffer IDs
 */
public class GLES11IdImpl implements GLId {
    private static int sNextId = 1;
    // Mutex for sNextId
    private static Object sLock = new Object();

    @Override
    public int generateTexture() {
        synchronized (sLock) {
            return sNextId++;
        }
    }

    @Override
    public void glGenBuffers(int n, int[] buffers, int offset) {
        synchronized (sLock) {
            while (n-- > 0) {
                buffers[offset + n] = sNextId++;
            }
        }
    }

    @Override
    public void glDeleteTextures(GL11 gl, int n, int[] textures, int offset) {
        synchronized (sLock) {
            gl.glDeleteTextures(n, textures, offset);
        }
    }

    @Override
    public void glDeleteBuffers(GL11 gl, int n, int[] buffers, int offset) {
        synchronized (sLock) {
            gl.glDeleteBuffers(n, buffers, offset);
        }
    }

    @Override
    public void glDeleteFramebuffers(GL11ExtensionPack gl11ep, int n, int[] buffers, int offset) {
        synchronized (sLock) {
            gl11ep.glDeleteFramebuffersOES(n, buffers, offset);
        }
    }


}
