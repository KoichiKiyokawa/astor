package fr.inria.astor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CommandExecuter {
  /**
   * 指定したディレクトリに移動してから、コマンドを実行 Reference)
   * https://chat-messenger.com/blog/java/runtime-getruntime-exec
   *
   * @param args                            実行するコマンド
   * @param relatilePathForWorkingDirectory 移動先のディレクトリ
   *
   * @return 実行結果の文字列
   *
   */
  public static String run(String[] args, String relatilePathForWorkingDirectory) {
    String retStr = "";
    try {
      String LINE_SEPA = "\n";
      ProcessBuilder pb = new ProcessBuilder(args);
      pb.directory(new File(relatilePathForWorkingDirectory));
      Process p = pb.start();
      // getInputStream()で結果が帰ってこないことがあったので、標準出力と標準エラーを混ぜる
      // https://qiita.com/shintaness/items/6dd91260726e555c49e5
      pb.redirectErrorStream(true);
      InputStream in = null;
      BufferedReader br = null;
      try {
        in = p.getInputStream();
        StringBuffer out = new StringBuffer();
        br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
          out.append(line + LINE_SEPA);
        }
        retStr = out.toString();
      } finally {
        if (br != null) {
          br.close();
        }
        if (in != null) {
          in.close();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return retStr;
  }

  public static String[] exec(String[] args, String relatilePathForWorkingDirectory) {
    ArrayList<String> resultLines = new ArrayList<>();
    try {
      Runtime runtime = Runtime.getRuntime();
      Process p = null;
      File dir = new File(relatilePathForWorkingDirectory);// 実行ディレクトリの指定
      try {
        p = runtime.exec(args, null, dir); // 実行ディレクトリ(dir)でargsを実行する
      } catch (IOException e) {
        e.printStackTrace();
      }

      try {
        p.waitFor(); // プロセスの正常終了まで待機させる
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      InputStream is = p.getInputStream(); // プロセスの結果を変数に格納する
      BufferedReader br = new BufferedReader(new InputStreamReader(is)); // テキスト読み込みを行えるようにする

      while (true) {
        String line = br.readLine();
        if (line == null) {
          break; // 全ての行を読み切ったら抜ける
        } else {
          resultLines.add(line);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return resultLines.toArray(new String[resultLines.size()]);
  }
}