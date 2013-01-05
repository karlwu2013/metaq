package com.taobao.metamorphosis.tools.shell;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import com.taobao.metamorphosis.tools.utils.CommandLineUtils;


/**
 * <pre>
 * �������(Ŀ¼)��ǰ�����ƫ��,һ�����ڷ���Ǩ��ʱ
 * usage:
 *      MovePartitionFiles -dataDir /home/admin/metadata -topic xxtopic -start 5 -end 10 -offset -5
 * </pre>
 * 
 * @author �޻�
 * @since 2011-8-25 ����12:59:24
 */

public class MovePartitionFiles extends ShellTool {

    public MovePartitionFiles(PrintStream out) {
        super(out);
    }


    public static void main(String[] args) throws Exception {
        new MovePartitionFiles(System.out).doMain(args);
    }


    @Override
    public void doMain(String[] args) throws Exception {
        CommandLine commandLine = this.getCommandLine(args);
        String dataDir = commandLine.getOptionValue("dataDir");
        String topic = commandLine.getOptionValue("topic");
        int start = Integer.parseInt(commandLine.getOptionValue("start"));
        int end = Integer.parseInt(commandLine.getOptionValue("end"));
        int offset = Integer.parseInt(commandLine.getOptionValue("offset"));

        this.checkArg(dataDir, topic, start, end, offset);

        List<File> oldPartitionPaths = this.getPartitionPaths(dataDir, topic, start, end);
        this.checkOldPartitionPaths(oldPartitionPaths);

        List<File> newPartitionPaths = this.getPartitionPaths(dataDir, topic, start + offset, end + offset);
        this.checkNewPartitionPaths(oldPartitionPaths, newPartitionPaths);

        this.rename(offset, oldPartitionPaths, newPartitionPaths);

    }


    private void rename(int offset, List<File> oldPartitionPaths, List<File> newPartitionPaths) {
        // ��ǰ�ƶ�
        if (offset < 0) {
            for (int i = 0; i < oldPartitionPaths.size(); i++) {
                oldPartitionPaths.get(i).renameTo(newPartitionPaths.get(i));
                this.println(oldPartitionPaths.get(i).getAbsolutePath() + " rename to " + newPartitionPaths.get(i));
            }
        }
        else {// ����ƶ�
            for (int i = oldPartitionPaths.size() - 1; i >= 0; i--) {
                if (oldPartitionPaths.get(i).renameTo(newPartitionPaths.get(i))) {
                    this.println(oldPartitionPaths.get(i).getAbsolutePath() + " rename to " + newPartitionPaths.get(i));

                }
                else {
                    this.println(oldPartitionPaths.get(i).getAbsolutePath() + " rename to " + newPartitionPaths.get(i)
                            + " failed");
                }
            }
        }
    }


    /** ����ƶ��������Ŀ¼�Ƿ��Ѿ�����,��һ�����ھͲ���������,�׳��쳣 */
    private void checkNewPartitionPaths(List<File> oldPartitionPaths, List<File> newPartitionPaths) {
        for (File file : newPartitionPaths) {
            if (!oldPartitionPaths.contains(file) && file.exists()) {
                throw new IllegalStateException("can not move," + "expected new dir " + file.getAbsolutePath()
                        + " exists");
            }
        }

    }


    /** �����Ҫ�ƶ���Ŀ¼�Ƿ����,��һ�������ھͲ���������,�׳��쳣 */
    private void checkOldPartitionPaths(List<File> oldPartitionPaths) {
        for (File file : oldPartitionPaths) {
            if (!file.exists()) {
                throw new RuntimeException("can not move,old partition dir " + file.getAbsolutePath() + " not exists");
            }
        }

    }


    private List<File> getPartitionPaths(String dataDir, String topic, int start, int end) {
        List<File> oldPartitionPaths = new LinkedList<File>();
        for (int i = start; i <= end; i++) {
            oldPartitionPaths.add(new File(dataDir + File.separator + topic + "-" + i));
        }
        return oldPartitionPaths;
    }


    private CommandLine getCommandLine(String[] args) {
        Option dataDirOption = new Option("dataDir", true, "meta data dir");
        dataDirOption.setRequired(true);
        Option topicOption = new Option("topic", true, "topic");
        topicOption.setRequired(true);
        Option startOption = new Option("start", true, "start partition number");
        startOption.setRequired(true);
        Option endOption = new Option("end", true, "end partition number");
        endOption.setRequired(true);
        Option offsetOption = new Option("offset", true, "���������ǰ�����ƫ����");
        offsetOption.setRequired(true);

        return CommandLineUtils.parseCmdLine(args, new Options().addOption(dataDirOption).addOption(topicOption)
            .addOption(startOption).addOption(endOption).addOption(offsetOption));
    }


    private void checkArg(String dataDir, String topic, int start, int end, int offset) {
        if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("can not move,topic is blank");
        }
        if (StringUtils.isBlank(dataDir)) {
            throw new IllegalArgumentException("can not move,dataDir is blank");
        }
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException("can not move,start and end must not less than 0");
        }

        if (start > end) {
            throw new IllegalArgumentException("can not move,start less then end");
        }
        if (offset == 0) {
            throw new IllegalArgumentException("can not move,offset == 0,don��t move");
        }
        if ((start + offset) < 0) {
            throw new IllegalArgumentException("can not move,�ƶ�����С�ķ�����Ž�С��0");
        }
    }

}