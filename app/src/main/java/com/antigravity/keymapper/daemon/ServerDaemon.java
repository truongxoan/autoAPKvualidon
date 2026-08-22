package com.antigravity.keymapper.daemon;

import android.os.IBinder;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * High-performance Zero-Detection Injection Server running inside ADB shell process.
 * Does not touch any game files, memory, or ART runtime.
 */
public class ServerDaemon {
    private static final int PORT = 62123;
    private static final int MAGIC = 0x4B45594D; // "KEYM"

    private static Method injectInputEventMethod;
    private static Object inputManagerInstance;

    public static void main(String[] args) {
        System.out.println("[AntiGravity Daemon] Starting Keymapper Server...");
        initInputManager();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[AntiGravity Daemon] Listening on port " + PORT);

            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    client.setTcpNoDelay(true);
                    System.out.println("[AntiGravity Daemon] Client connected: " + client.getRemoteSocketAddress());
                    handleClient(client);
                } catch (Exception e) {
                    System.err.println("[AntiGravity Daemon] Client error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initInputManager() {
        try {
            // Access ServiceManager.getService("input")
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getServiceMethod = serviceManagerClass.getMethod("getService", String.class);
            IBinder binder = (IBinder) getServiceMethod.invoke(null, "input");

            // Access IInputManager.Stub.asInterface(binder)
            Class<?> stubClass = Class.forName("android.hardware.input.IInputManager$Stub");
            Method asInterfaceMethod = stubClass.getMethod("asInterface", IBinder.class);
            inputManagerInstance = asInterfaceMethod.invoke(null, binder);

            // Access injectInputEvent(InputEvent, int)
            for (Method m : inputManagerInstance.getClass().getMethods()) {
                if (m.getName().equals("injectInputEvent") && m.getParameterTypes().length >= 2) {
                    injectInputEventMethod = m;
                    break;
                }
            }
            System.out.println("[AntiGravity Daemon] InputManager reflection initialized successfully.");
        } catch (Exception e) {
            System.err.println("[AntiGravity Daemon] Failed to initialize InputManager via reflection: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (InputStream in = socket.getInputStream()) {
            byte[] headerBuffer = new byte[14];

            while (true) {
                int read = 0;
                while (read < 14) {
                    int r = in.read(headerBuffer, read, 14 - read);
                    if (r == -1) return; // disconnected
                    read += r;
                }

                ByteBuffer bb = ByteBuffer.wrap(headerBuffer).order(ByteOrder.LITTLE_ENDIAN);
                int magic = bb.getInt();
                if (magic != MAGIC) {
                    continue;
                }

                int slot = bb.get() & 0xFF;
                int action = bb.get() & 0xFF;
                float x = bb.getFloat();
                float y = bb.getFloat();

                injectTouchEvent(slot, action, x, y);
            }
        } catch (Exception e) {
            // Client disconnected
        }
    }

    private static final MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[10];
    private static final MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[10];
    private static long downTime = 0;

    static {
        for (int i = 0; i < 10; i++) {
            pointerProperties[i] = new MotionEvent.PointerProperties();
            pointerProperties[i].id = i;
            pointerProperties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;

            pointerCoords[i] = new MotionEvent.PointerCoords();
            pointerCoords[i].pressure = 1.0f;
            pointerCoords[i].size = 1.0f;
        }
    }

    private static void injectTouchEvent(int slot, int actionType, float x, float y) {
        long now = SystemClock.uptimeMillis();
        if (actionType == 0) { // DOWN
            downTime = now;
        }

        pointerCoords[slot].x = x;
        pointerCoords[slot].y = y;

        int motionAction;
        switch (actionType) {
            case 0:
                motionAction = MotionEvent.ACTION_DOWN;
                break;
            case 1:
                motionAction = MotionEvent.ACTION_UP;
                break;
            default:
                motionAction = MotionEvent.ACTION_MOVE;
                break;
        }

        MotionEvent event = MotionEvent.obtain(
                downTime,
                now,
                motionAction,
                1,
                pointerProperties,
                pointerCoords,
                0,
                0,
                1.0f,
                1.0f,
                0,
                0,
                InputDevice.SOURCE_TOUCHSCREEN,
                0
        );

        if (event != null && injectInputEventMethod != null && inputManagerInstance != null) {
            try {
                // injectInputEvent(event, 0 (INJECT_INPUT_EVENT_MODE_ASYNC))
                injectInputEventMethod.invoke(inputManagerInstance, event, 0);
            } catch (Exception e) {
                // ignore
            } finally {
                event.recycle();
            }
        }
    }
}
