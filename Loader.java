import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class Loader extends OutputStream {

    private ScriptWindow scriptwindow;
    private String url;
    private String mainclass;

    public static void main(String[] args) throws Exception {
        Loader l = new Loader("file:Minecraft.jar", "net.minecraft.LauncherFrame");
        l.load(args);
    }

    public void load(String[] args) throws Exception {
        scriptwindow.addline("D args: " + Arrays.toString(args));
        scriptwindow.addline("D os.name: " + System.getProperty("os.name"));
        scriptwindow.addline("D user.home: " + System.getProperty("user.home"));
        scriptwindow.addline("D %APPDATA%: " + System.getenv("APPDATA"));
        scriptwindow.addline("D $HOME: " + System.getenv("HOME"));
        scriptwindow.addline("D $USER: " + System.getenv("USER"));
        //".minecraft";

        scriptwindow.addline("= Loading: " + url);
        URLClassLoader cl = new URLClassLoader(new URL[]{new URL("jar:" + url + "!/")});
        Class cls = cl.loadClass(mainclass);
        scriptwindow.addline("= Class: " + mainclass);
        Object obj = cls.newInstance();
        scriptwindow.addObject("mainclass", obj, false);
        Frame frame = (Frame) obj;
        frame.setVisible(true);

        Field field = null;
        for (Field zz : obj.getClass().getDeclaredFields()) {
            if (zz.getName().equalsIgnoreCase("launcher")) {
                field = zz;
                break;
            }
        }
        if (field == null) {
            scriptwindow.addline("@ NO LAUNCHER FIELD!");
            return;
        }
        field.setAccessible(true);

        while (field.get(obj) == null)
            Thread.sleep(500);

        Object object = field.get(obj);

        scriptwindow.addObject("launcher", object, false);

        field = null;
        for (Field zz : object.getClass().getDeclaredFields()) {
            if (zz.getName().equalsIgnoreCase("applet")) {
                field = zz;
                break;
            }
        }
        if (field == null) {
            scriptwindow.addline("@ NO APPLET FIELD!");
            return;
        }
        field.setAccessible(true);

        while (field.get(object) == null)
            Thread.sleep(500);

        object = field.get(object);

        scriptwindow.addObject("applet", object, false);

        field = null;
        for (Field zz : object.getClass().getDeclaredFields()) {
            if (zz.getType().toString().toLowerCase().endsWith("minecraft")) {
                field = zz;
                break;
            }
        }
        if (field == null) {
            scriptwindow.addline("@ NO MINECRAFT FIELD!");
            return;
        }
        field.setAccessible(true);

        while (field.get(object) == null)
            Thread.sleep(500);

        scriptwindow.addObject("game", field.get(object), true);
    }

    public Loader(String url, String mainclass) throws Exception {
        this.url = url;
        this.mainclass = mainclass;
        PrintStream p = new PrintStream(this);
        //System.setErr(p);
        //System.setOut(p);
        scriptwindow = new ScriptWindow("Minecraft Hax by eXemplar");
        scriptwindow.addObject("loader", this, false);
        scriptwindow.setVisible(true);
    }

    private String last = "";

    @Override
    public void write(int b) throws IOException {
        if ( b == '\n' ) {
            scriptwindow.addline("- " + last);
            last = "";
        } else {
            last += (char)b;
        }
    }

}
