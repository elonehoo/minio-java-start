package org.example.minio.start.api;

import cn.hutool.core.io.resource.FileResource;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.UrlResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.example.minio.start.entity.MinioFileEntity;
import org.example.minio.start.entity.MinioShareFileEntity;
import org.example.minio.start.utils.ApiUtil;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhy
 * @email 2434017367@qq.com
 * @date 2022/6/19 12:24
 */
public class MinioFileApi {

    private static String getReqUri(String uri) {
        return "files/" + uri;
    }

    /**
     * 临时文件保存路径
     */
    public static final String INTERIM_SAVE_PATH = "interim";

    /**
     * 文件上传
     * @param resource
     * @param savePath 文件存放路径
     * @return
     */
    public static String uploadFile(Resource resource, String savePath) {
        HttpRequest httpRequest = ApiUtil.getHttpRequest(Method.POST, getReqUri("upload"));

        httpRequest.form("file", resource);
        if (StrUtil.isNotEmpty(savePath)) {
            httpRequest.form("path", savePath);
        }

        HttpResponse httpResponse = ApiUtil.executeHttpRequest(httpRequest);
        JSONObject jsonObject = ApiUtil.getResponseResult(httpResponse);
        String fileId = jsonObject.getStr("data");

        return fileId;
    }

    /**
     * 文件上传
     * @param filePath
     * @param savePath
     * @return
     */
    public static String uploadFile(String filePath, String savePath) {
        return uploadFile(new FileResource(filePath), savePath);
    }

    /**
     * 文件上传
     * @param fileUrl
     * @param filename
     * @param savePath
     * @return
     */
    public static String uploadFile(String fileUrl, String filename, String savePath) throws MalformedURLException {
        UrlResource urlResource = new UrlResource(new URL(fileUrl), filename);
        return uploadFile(urlResource, savePath);
    }

    /**
     * 下载文件
     * @param fileId
     * @return
     */
    private static HttpResponse downloadFile(String fileId) {
        HttpRequest httpRequest = ApiUtil.getHttpRequest(Method.GET, getReqUri("download"));
        httpRequest.form("fileId", fileId);
        HttpResponse httpResponse = ApiUtil.executeHttpRequest(httpRequest);
        return httpResponse;
    }

    public static void downloadFile(String fileId, String targetFilePath) {
        HttpResponse httpResponse = downloadFile(fileId);

        httpResponse.writeBody(targetFilePath);
    }

    public static void downloadFile(String fileId, OutputStream outputStream) {
        HttpResponse httpResponse = downloadFile(fileId);

        httpResponse.writeBody(outputStream, true, null);
    }

    /**
     * word转pdf
     * @param wordPath
     * @param pdfPath
     */
    public static void wordToPdf(String wordPath, String pdfPath) {
        HttpRequest httpRequest = ApiUtil.getHttpRequest(Method.POST, getReqUri("wordToPdf"));

        httpRequest.form("file", new FileResource(wordPath));

        HttpResponse httpResponse = ApiUtil.executeHttpRequest(httpRequest);

        httpResponse.writeBody(pdfPath);
    }

    /**
     * 删除文件
     * @param fileId
     * @return
     */
    public static void delFile(String fileId) {
        HttpRequest httpRequest = ApiUtil.getHttpRequest(Method.DELETE, "delFile");

        httpRequest.form("fileId", fileId);

        ApiUtil.executeHttpRequest(httpRequest);
    }

    /**
     * 获取文件列表
     * @param fileIds
     * @return
     */
    public static List<MinioFileEntity> getFileList(String fileIds) {
        HttpRequest httpRequest = ApiUtil.getHttpRequest(Method.GET, getReqUri("getListByIds"));

        httpRequest.form("ids", fileIds);

        HttpResponse httpResponse = ApiUtil.executeHttpRequest(httpRequest);

        JSONObject jsonObject = ApiUtil.getResponseResult(httpResponse);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<MinioFileEntity> entityList = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject fileJsonObject = jsonArray.getJSONObject(i);
            MinioFileEntity minioFileEntity = new MinioFileEntity();
            minioFileEntity.setFileId(fileJsonObject.getStr("id"));
            minioFileEntity.setFileName(fileJsonObject.getStr("fileName"));
            minioFileEntity.setFileSuffix(fileJsonObject.getStr("fileSuffix"));
            entityList.add(minioFileEntity);
        }

        return entityList;
    }

    public static List<MinioFileEntity> getFileList(Collection<String> fileIdList) {
        String fileIds = fileIdList.stream().collect(Collectors.joining(","));

        return getFileList(fileIds);
    }

    public static List<MinioFileEntity> getFileList(String[] fileIdArr) {
        String fileIds = Arrays.stream(fileIdArr).collect(Collectors.joining(","));

        return getFileList(fileIds);
    }

    /**
     * 清除临时文件
     */
    public static void clearInterim() {
        HttpRequest httpRequest = ApiUtil.getHttpRequest(Method.GET, getReqUri("clearInterim"));

        ApiUtil.executeHttpRequest(httpRequest);
    }

    /**
     * 获取文件分享链接
     * @param fileId
     * @param second
     * @return
     */
    public static MinioShareFileEntity getShareFile(String fileId, long second) {
        HttpRequest httpRequest = ApiUtil.getHttpRequest(Method.GET, getReqUri("getShareFile"));

        httpRequest.form("fileId", fileId);
        httpRequest.form("second", second);

        HttpResponse httpResponse = ApiUtil.executeHttpRequest(httpRequest);
        JSONObject jsonObject = ApiUtil.getResponseResult(httpResponse);
        JSONObject data = jsonObject.getJSONObject("data");

        MinioShareFileEntity minioShareFileEntity = new MinioShareFileEntity();
        minioShareFileEntity.setFileUrl(data.getStr("fileUrl"));
        minioShareFileEntity.setFileName(data.getStr("fileName"));

        return minioShareFileEntity;
    }

    /**
     * 获取文件分享链接 永久
     * @param fileId
     * @return
     */
    public static MinioShareFileEntity getShareFile(String fileId) {
        return getShareFile(fileId, -1);
    }

}
