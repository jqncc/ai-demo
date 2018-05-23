package com.ghgcn.ai.client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ghgcn.ai.AiFaceClient;
import com.qcloud.image.ImageClient;
import com.qcloud.image.request.FaceAddFaceRequest;
import com.qcloud.image.request.FaceAddGroupIdsRequest;
import com.qcloud.image.request.FaceDelPersonRequest;
import com.qcloud.image.request.FaceGetPersonIdsRequest;
import com.qcloud.image.request.FaceIdentifyRequest;
import com.qcloud.image.request.FaceNewPersonRequest;

public class TencentAiFaceClient implements AiFaceClient {

    private String bucketName = "face-1253975133";
    private String appId = "10132068";
    private String SecretId = "AKIDBHGQGSlVVj1z44QXF7RsXhjw0zOeMsml";
    private String SecretKey = "WTZxBQVfw9eh0A91kRuMhy6d4jl9faVb";
    ImageClient imageClient = null;

    public TencentAiFaceClient() {
        imageClient = new ImageClient(appId, SecretId, SecretKey);
    }

    @Override
    public void addUser() {
        // 一个 person 最多允许包含 20 个 face
        String ret;
        FaceNewPersonRequest personNewReq;
        String[] groupIds = new String[2];
        groupIds[0] = "sz";
        groupIds[1] = "shop001";
        String personName = "yangmi1";
        String personId = "personId1";
        String personTag = "star1";

        /*
        // 1. url方式
        System.out.println("====================================================");
        String personNewUrl = "YOUR URL";
        personNewReq = new FaceNewPersonRequest(bucketName, personId, groupIds, personNewUrl, personName, personTag);
        ret = imageClient.faceNewPerson(personNewReq);
        System.out.println("person new  ret:" + ret);*/

        // 2. 图片内容方式
        /*File personNewImage = new File("E:\\codeback\\image-java-sdk-v2.0-2.2.6\\assets", "icon_porn04.jpg");
        personNewReq = new FaceNewPersonRequest(bucketName, personId, groupIds, personNewImage, personName, personTag);
        ret = imageClient.faceNewPerson(personNewReq);
        // {"code":0,"message":"OK","data":{"person_id":"personId1","suc_group":2,"suc_face":1,"session_id":"","face_id":"2589490809310030953","group_ids":["shop001","sz"]}}
        System.out.println("person new ret:" + ret);*/

        /* // 3. 图片内容方式(byte[])
        System.out.println("====================================================");
        byte[] imageContent = getFileBytes(personNewImage);
        if (imageContent != null) {
            personNewReq = new FaceNewPersonRequest(bucketName, personId, groupIds, imageContent, personName,
                    personTag);
            ret = imageClient.faceNewPerson(personNewReq);
            System.out.println("person new ret:" + ret);
        } else {
            System.out.println("person new ret: get image content fail");
        }*/

        File faceDir = new File("C:\\Users\\yucan.zhang\\Pictures\\faces");
        String[] faceNames = faceDir.list();
        System.out.println("face photo count:" + faceNames.length);
        File[] faceFiles = faceDir.listFiles();
        Map<String,String> names = new HashMap<>();
        long s = System.nanoTime();
        for (File file : faceFiles) {
            long s1 = System.nanoTime();
            String name = file.getName().substring(0, file.getName().lastIndexOf(".")).split("_")[0];
            if (names.containsKey(name)) {
                System.out.println("add face to :" + name);
                String[] addPhotoNames = new String[]{ name };
                File[] addPhotoFiles = new File[]{ file };
                FaceAddFaceRequest addFaceReq = new FaceAddFaceRequest(bucketName, addPhotoNames, addPhotoFiles,
                        names.get(name), file.getName());
                ret = imageClient.faceAddFace(addFaceReq);
                // JSONObject jobj = new JSONObject(ret);
            } else {
                System.out.println("add user :" + name);
                String uid = UUID.randomUUID().toString();
                personNewReq = new FaceNewPersonRequest(bucketName, uid, groupIds, file, name, file.getName());
                ret = imageClient.faceNewPerson(personNewReq);
                JSONObject jobj = new JSONObject(ret);
                if (jobj.getInt("code") == 0) {
                    names.put(name, uid);
                }
            }
            System.out.println(ret);
            long e1 = System.nanoTime() - s1;
            System.out.println("elapsed time:" + TimeUnit.NANOSECONDS.toMillis(e1));
        }
        long e = System.nanoTime() - s;
        System.out.println("total time:" + TimeUnit.NANOSECONDS.toSeconds(e) + " , okuser:" + names.size());
        // 154张照片,105个用户.每用户注册在320-1650ms,总耗时130-144s
        // 模糊照片检测不到人脸zhangdongliang
    }

    @Override
    public void addGroup() {
        String ret;
        FaceAddGroupIdsRequest request = new FaceAddGroupIdsRequest(bucketName, "personId1", "group2");
        ret = imageClient.faceAddGroupIds(request, false);
        System.out.println("face add group ids  ret:" + ret);
    }

    @Override
    public void detect() {
    }

    @Override
    public void findFaces() {
    }

    @Override
    public void getUser() {
    }

    @Override
    public void getFaces() {
    }

    @Override
    public void match() {
        String ret;

        // 2. 图片内容方式
        long s = System.nanoTime();
        File faceImageFile = new File("C:\\Users\\yucan.zhang\\Pictures\\compare_faces");
        File[] files = faceImageFile.listFiles();
        for (File file : files) {
            long s1 = System.nanoTime();
            FaceIdentifyRequest faceIdentifyReq2 = new FaceIdentifyRequest(bucketName, "sz", file);// 一个 groupId
            // FaceIdentifyRequest faceIdentifyReq2 = new FaceIdentifyRequest(bucketName, groupIds, faceImageFile);// 多个
            // groupId
            ret = imageClient.faceIdentify(faceIdentifyReq2);
            System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - s1));
            System.out.println(String.format("photo:%s compare result:", file.getName()));
            JSONObject jobj = new JSONObject(ret);
            if (jobj.getInt("code") == 0) {
                JSONArray dataNodes = jobj.getJSONObject("data").getJSONArray("candidates");
                Iterator<Object> it = dataNodes.iterator();
                while (it.hasNext()) {
                    JSONObject obj = (JSONObject) it.next();
                    String out = String.format("===>%s 置信度:%s, pid:%s,fid:%s,", obj.getString("tag"),
                            obj.getDouble("confidence"), obj.getString("person_id"), obj.getString("face_id"));
                    System.out.println(out);
                }
            } else {
                System.out.println(ret);
            }
        }

        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - s));
        // 23张照片,人脸库151张,比对耗时:320-800ms每张,共4s
    }

    @Override
    public void persionVerify() {
    }

    public void deleteUser(String pid) {
        FaceDelPersonRequest request = new FaceDelPersonRequest(bucketName, pid);
        String ret = imageClient.faceDelPerson(request);
        // {"code":0,"message":"OK","data":{"deleted":1,"session_id":"","person_id":"7a8c0feb-6398-4dc0-8b13-d41470fa8552"}}
        System.out.println(ret);
    }

    public void deleteUsersByGroupId(String groupId) {
        FaceGetPersonIdsRequest request = new FaceGetPersonIdsRequest(bucketName, groupId);
        String ret = imageClient.faceGetPersonIds(request);
        JSONObject jobj = new JSONObject(ret);
        JSONArray jarr = jobj.getJSONObject("data").getJSONArray("person_ids");
        Iterator<Object> it = jarr.iterator();
        while (it.hasNext()) {
            String pid = (String) it.next();
            FaceDelPersonRequest delrequest = new FaceDelPersonRequest(bucketName, pid);
            String delret = imageClient.faceDelPerson(delrequest);
            System.out.println(delret);
        }

        System.out.println(ret);
    }

    public static void main(String[] args) throws IOException {
        TencentAiFaceClient client = new TencentAiFaceClient();
        // client.addUser();
        client.match();

        // 删除用户
        // client.deleteUsersByGroupId("sz");
    }
}
