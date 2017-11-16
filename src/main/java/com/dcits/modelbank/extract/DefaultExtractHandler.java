package com.dcits.modelbank.extract;

import com.dcits.modelbank.model.FileModel;
import com.dcits.modelbank.utils.DateUtil;
import com.dcits.modelbank.utils.FileUtil;
import com.dcits.modelbank.utils.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created on 2017-11-10 16:14.
 *
 * @author kevin
 */
public class DefaultExtractHandler extends PatchExtractHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultExtractHandler.class);

    @Override
    protected void fileTransfer(Set<String> set) {
        logger.info("target目录：" + super.targetDir);
        logger.info("result目录：" + super.resultDir);
        // 将增量jar包列表输出到文件
        FileUtil.writeFile(resultDir + "/" + DateUtil.getRunDate() + ".txt", set.toString().replace(", ", "\n"));
        FileUtil.filterFile(targetDir, set);
        String fileName = "patch-" + DateUtil.getRunDate() + ".zip";
        try {
            ZipUtil.zip(targetDir, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String jarName : set) {
            logger.info(jarName);
        }
    }

    @Override
    protected Set<String> getAllPackageName(List<FileModel> list) {
        Set<String> set = new HashSet<>();
        for (FileModel file : list) {
            String filePath = this.sourceDir + file.getPath();
            if (!isFileInPackage(filePath)) continue;

            String pomPath = FileUtil.findFilePath(filePath, "pom.xml");
            if (Objects.equals(null, pomPath) || Objects.equals("", pomPath)) continue;
            String packageName = xmlBulider.pom2PackageName(pomPath);
            set.add(packageName);
        }

        logger.info("增量文件所在的jar包数量为：" + set.size());
        return set;
    }
}