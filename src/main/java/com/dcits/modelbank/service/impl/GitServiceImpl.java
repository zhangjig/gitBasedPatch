package com.dcits.modelbank.service.impl;

import com.dcits.modelbank.extract.BasePatchExtractHandler;
import com.dcits.modelbank.jgit.GitHandler;
import com.dcits.modelbank.jgit.GitHandlerImpl;
import com.dcits.modelbank.jgit.helper.GitHelper;
import com.dcits.modelbank.model.FileDiffEntry;
import com.dcits.modelbank.model.FileModel;
import com.dcits.modelbank.service.GitService;
import com.dcits.modelbank.utils.Const;
import com.dcits.modelbank.utils.FileUtil;
import com.dcits.modelbank.utils.XmlBulider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

/**
 * Created on 2017-11-07 19:42.
 *
 * @author kevin
 */
public class GitServiceImpl implements GitService {
    private static final Logger logger = LoggerFactory.getLogger(GitServiceImpl.class);

    private GitHandler gitHandler;
    @Resource
    private XmlBulider xmlBulider;
    @Resource
    private BasePatchExtractHandler basePatchExtractHandler;

    public GitServiceImpl(GitHandler gitHandler) {
        this.gitHandler = gitHandler;
    }

    @Override
    public void genChangesFileListToday() {
        List<FileModel> fileModelList = new ArrayList<>();
        Map<String, List<FileDiffEntry>> lists = gitHandler.getCommitsLogByFile();
        for (String key : lists.keySet()) {
            List<FileDiffEntry> list = lists.get(key);
            FileModel fileModel = diffEntry2FileModel(list);
            fileModelList.add(fileModel);
        }
        xmlBulider.entity2XmlFile(fileModelList);
    }

    @Override
    public void genChangesFileListBetweenTag(String basePath, String tagStart, String tagEnd) {
        List<FileModel> fileModelList = new ArrayList<>();
        Map<String, List<FileDiffEntry>> lists = gitHandler.getCommitsLogByFile(tagStart, tagEnd);
        for (String key : lists.keySet()) {
            logger.info("File:" + key);
            List<FileDiffEntry> list = lists.get(key);
            FileModel fileModel = diffEntry2FileModel(list);
            fileModelList.add(fileModel);
        }
        xmlBulider.entity2XmlFile(fileModelList);
    }

    @Override
    public void patchFileExecute() {
        basePatchExtractHandler.execute();
    }

    @Override
    public void getCommitTimeByTag(String tagName) {
        gitHandler.commitTimeOfTag(tagName);
    }

    /**
     * 实体类之间的转换
     *
     * @param entries
     * @return
     */
    public FileModel diffEntry2FileModel(List<FileDiffEntry> entries) {
        FileModel fileModel = new FileModel();
        if (entries.size() < 1) return fileModel;
        Iterator<FileDiffEntry> iterator = entries.iterator();
        FileDiffEntry entry = iterator.next();
        fileModel.setName(entry.getPkgPath());
        fileModel.setType(entry.getType());
        fileModel.setModule(entry.getModule());
        fileModel.setPath(entry.getFullPath());
        Map<String, String> map = fileDiffEntry2Map(entry);
        List<Map<String, String>> list = new ArrayList<>();
        list.add(map);
        while (iterator.hasNext()) {
            entry = iterator.next();
            map = fileDiffEntry2Map(entry);
            list.add(map);
        }
        fileModel.setAuthors(list);
        return fileModel;
    }

    private Map<String, String> fileDiffEntry2Map(FileDiffEntry entry) {
        Map<String, String> map = new HashMap<>();
        map.put(Const.AUTHOR_NAME, entry.getAuthor());
        map.put(Const.VERSION_ID, entry.getVersion());
        map.put(Const.TIMESTAMP, entry.getTimestamp());
        String desc = entry.getDesc();
        if (desc.endsWith("\n")) desc = desc.substring(0, desc.length() - 1);
        map.put(Const.DESC, desc);
        map.put(Const.CHANGE_TYPE, entry.getChangeType());
        map.put(Const.CHECK, "true");
        return map;
    }

}
