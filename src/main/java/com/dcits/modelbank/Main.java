package com.dcits.modelbank;

import com.dcits.modelbank.extract.BasePatchExtractHandler;
import com.dcits.modelbank.jgit.helper.GitCollector;
import com.dcits.modelbank.jgit.helper.GitHelper;
import com.dcits.modelbank.service.GitService;
import com.dcits.modelbank.utils.XmlBulider;
import com.sun.xml.internal.bind.v2.TODO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created on 2017-11-15 15:34.
 *
 * @author kevin
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private ApplicationContext context;
    private GitCollector gitCollector;

    private GitService gitService;
    private final String gitDir = ".git"
            .concat(File.separator)
            .concat("modules")
            .concat(File.separator)
            .concat("SmartEnsemble");
    private final String source = "SmartEnsemble";
    private final String resule = "modules"
            .concat(File.separator)
            .concat("modelBank-all-integration")
            .concat(File.separator)
            .concat("target");
    private final String target = this.resule
            .concat(File.separator)
            .concat("modelBank-integration-assembly")
            .concat(File.separator)
            .concat("modelBank-integration");

    private Main(String[] paths) {
        // 接收传入的绝对路径
        String baseDir = paths[1].trim();
        baseDir = baseDir.endsWith(File.separator) ? baseDir : baseDir + File.separator;
        this.context = new ClassPathXmlApplicationContext("classpath*:applicationContext.xml");
        gitCollector = context.getBean(GitCollector.class);
        Map<String, GitHelper> map = gitCollector.getCollectors();
        Iterator<Map.Entry<String, GitHelper>> iterator =  map.entrySet().iterator();

        /**
         * 一个GitHelper指定一个本地Git配置库：
         * 1、gitHelper的.git日志文件夹相对路径改为绝对路径
         * 2、gitHelper的源码跟目录相对路径转为绝对路径（相对路径@标识当前路径）
         */
        GitHelper gitHelper;
        while (iterator.hasNext()) {
            Map.Entry<String, GitHelper> entry = iterator.next();
            gitHelper = entry.getValue();
            gitHelper.setRootDir(baseDir + gitHelper.getRootDir());
            String sourceDir = gitHelper.getSourceDir();
            gitHelper.setSourceDir(baseDir + (Objects.equals("@",sourceDir)?"":sourceDir));
        }

        // TODO: 2017/12/6 xmlBulider属性设置
//        XmlBulider xmlBulider = context.getBean(XmlBulider.class);
//        xmlBulider.setXmlFilePath(resultDir);
//        // 输入输出目录的设定
//        BasePatchExtractHandler extractHandler = context.getBean(BasePatchExtractHandler.class);
//        extractHandler.setSourceDir(sourceDir);
//        extractHandler.setTargetDir(targetDir);
//        extractHandler.setResultDir(resultDir);

        // 获取类实例
        gitService = context.getBean(GitService.class);
    }

    public static void main(String[] args) {
        if (args.length > 4 || args.length < 3) {
            System.out.println("请指定命令参数，默认操作命令如下：[xml/zip] [gitDir] [sourceDir] [targetDir] [resultDir]");
            System.out.println("xml：生成增量描述文件/zip：进行增量文件抽取；");
            System.out.println("baseDir：抽取增量项目跟路径");
            System.out.println("beginTag：抽取增量起始Tag；");
            System.out.println("endTag：抽取增量结束Tag【该参数为空则取到当前时间的记录】");
            return;
        }
        Main main = new Main(args);
        String cmd = args[0].trim();
        String startTag = args[2].trim();
        String endTag = args.length == 4 ? args[3].trim() : null;
        switch (cmd) {
            case "xml":
                main.gitService.genChangesFileListBetweenTag(startTag, endTag);
                System.out.println("增量描述文件抽取生成完毕！");
                break;
            case "zip":
                main.gitService.patchFileExecute();
                System.out.println("增量包生成完毕！");
                break;
            default:
                System.out.println("输入指令超出范围！");
                break;
        }
    }
}
