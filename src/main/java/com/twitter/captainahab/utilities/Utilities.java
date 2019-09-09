package com.twitter.captainahab.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class Utilities {

  private static File createExecutableFile(String prefix, String suffix) throws IOException {
    Set<PosixFilePermission> executable = PosixFilePermissions.fromString("rwxr--r--");
    FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(executable);

    Path path = Files.createTempFile(Paths.get("/tmp"), prefix, suffix, permissions);
    return new File(path.toAbsolutePath().toString());
  }

  public static String executeCommandLocally(String command, boolean sync) throws IOException {
    File file = createExecutableFile("command", ".sh");
    BufferedWriter bw = new BufferedWriter(new FileWriter(file));

    String data = "#!/usr/bin/env bash\n";
    data = data + command + "\n";

    bw.write(data);
    bw.close();

    String result = executeCommand("sudo " + file.getAbsolutePath(), sync);
//    file.delete();
    return result;
  }

  public static String executeCommandLocallyNonRoot(String command, boolean sync) throws IOException {
    File file = createExecutableFile("command", ".sh");
    BufferedWriter bw = new BufferedWriter(new FileWriter(file));

    String data = "#!/usr/bin/env bash\n";
    data = data + command + "\n";

    bw.write(data);
    bw.close();

    String result = executeCommand(file.getAbsolutePath(), sync);
//    file.delete();
    return result;
  }


  /*
   * Executing commands remotely would make more sense using a library such as JSch.
   * However, I could not make JSch work with Twitter's GCP.
   */
  public static String executeCommandRemotely(String host, String username, String command)
      throws IOException {
    File file = createExecutableFile("command", ".sh");
    BufferedWriter bw = new BufferedWriter(new FileWriter(file));

    String data = "#!/usr/bin/env bash\n";

    String sshTemplate = "ssh %s@%s '%s'\n";
    data = data + String.format(sshTemplate, username, host, command) + "\n";

    bw.write(data);
    bw.close();

    String result = executeCommand(file.getAbsolutePath(), true);
//    file.delete();
    return result;
  }

  // https://stackoverflow.com/questions/4750470/how-to-get-pid-of-process-ive-just-started-within-java-program
  public static synchronized long getPidOfProcess(Process p) {
    long pid = -1;

    try {
      if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
        Field f = p.getClass().getDeclaredField("pid");
        f.setAccessible(true);
        pid = f.getLong(p);
        f.setAccessible(false);
      }
    } catch (Exception e) {
      pid = -1;
    }
    return pid;
  }


  private static long executeCommandAsync(String command) {
    final Process p;

    long pid = -1;
    try {
      p = Runtime.getRuntime().exec(command);
      pid = getPidOfProcess(p);

      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            p.waitFor();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }).start();
    } catch (Exception e) {
      System.out.println(String.format("Error: %s for command: %s", e, command));
    }

    return pid;
  }

  private static String executeCommandSync(String command) {
    Process p;

    StringBuffer line = new StringBuffer();

    try {
      p = Runtime.getRuntime().exec(command);

      BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

      String tmpLine = input.readLine();
      while (tmpLine != null) {
        line.append(tmpLine);
        line.append('\n');
        tmpLine = input.readLine();
      }

      p.waitFor();
    } catch (Exception e) {
      return String.format("Error: %s for command: %s", e, command);
    }

    return line.toString();
  }

  private static String executeCommand(String command, boolean wait) {
    if (wait) {
      return executeCommandSync(command);
    }
    else {
      long pid = executeCommandAsync(command);
      return String.valueOf(pid);
    }
  }

  public static String scpTo(String localPath, String remotePath, String username, String host) throws IOException {
    String command = String.format("scp %s %s@%s:%s", localPath, username, host, remotePath);
    return Utilities.executeCommandLocallyNonRoot(command, true);
  }

}
