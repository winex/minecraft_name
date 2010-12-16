import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import javax.swing.JOptionPane;

public class Loader extends OutputStream {

    private ScriptWindow scriptwindow;
    private String url;
    private String mainclass;

    public String dir_install;

    public static void main(String[] args) throws Exception {
        String dir = searchInstallDir("minecraft");
        if (dir == null)
        {
            String err = "Failed to find installation directory!";
            System.err.println(err);
            JOptionPane.showMessageDialog(null, err, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        Loader l = new Loader("file:" + dir + System.getProperty("file.separator") + "Minecraft.jar", "net.minecraft.LauncherFrame", dir);
        l.load();
    }

    public static String searchInstallDir(String appname) throws Exception {
        String dirs[] = {
            System.getProperty("user.home") + "/.",
            System.getProperty("user.home") + "/Library/Application Support/",
            System.getenv("APPDATA") + System.getProperty("file.separator") + ".",
        };
        String s = null;

        for (int i = 0; i < dirs.length; i++)
        {
            s = dirs[i] + appname;
            File path = new File(s);
            if (path.isDirectory())
                return s;
        }
        return null;
    }

    public void load() throws Exception {
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

    public Loader(String url, String mainclass, String dir) throws Exception {
        this.url = url;
        this.mainclass = mainclass;
        this.dir_install = dir;

        PrintStream p = new PrintStream(this);
        //System.setErr(p);
        //System.setOut(p);

        scriptwindow = new ScriptWindow("Minecraft Name Changer by NanoEntity, modified code from eXemplar, updated by winex");
        scriptwindow.addObject("loader", this, false);

        scriptwindow.addline("D os.name: " + System.getProperty("os.name"));
        scriptwindow.addline("D user.home: " + System.getProperty("user.home"));
        scriptwindow.addline("D %APPDATA%: " + System.getenv("APPDATA"));
        scriptwindow.addline("D $HOME: " + System.getenv("HOME"));
        scriptwindow.addline("D $USER: " + System.getenv("USER"));

        String cfg = dir + System.getProperty("file.separator") + "name.txt";
        Properties props_new = new Properties();
        Properties props_cur = new Properties();

        props_cur.setProperty("name", "");
        props_cur.setProperty("hidescriptwindow", "false");
        props_cur.setProperty("noprompt", "false");

        // copy props_cur to have all keys (w/o defaults)
        props_new.putAll(props_cur);

        try {
            props_new.load(new FileInputStream(cfg));
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            scriptwindow.addline("E " + e.getMessage());
        }

        for (String k: props_new.stringPropertyNames())
        {
            if (props_cur.containsKey(k))
                continue;

            scriptwindow.addline("E uknown option: '" + k + "'");
            props_new.remove(k);
       }

        if (props_new.getProperty("name") == null)
            props_new.setProperty("name", "");

        // copy props_new to track changes
        props_cur.putAll(props_new);


        if (!props_new.getProperty("noprompt").equals("true"))
        {
            String s = props_cur.getProperty("name");
            s = JOptionPane.showInputDialog(null, "Minecraft Name:", s);
            if (s == null)
                System.exit(-1);
            props_new.setProperty("name", s);
        }

        scriptwindow.setVisible(!props_new.getProperty("hidescriptwindow").equals("true"));


        // save props if changed
        if (!props_new.get("name").equals(props_cur.get("name")) ||
            !props_new.get("hidescriptwindow").equals(props_cur.get("hidescriptwindow")) ||
            !props_new.get("noprompt").equals(props_cur.get("noprompt")) )
        {
            try {
                props_new.store(new FileOutputStream(cfg), null);
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
                scriptwindow.addline("E " + e.getMessage());
            }
        }

        scriptwindow.nameSetting(props_new.getProperty("name"));
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
