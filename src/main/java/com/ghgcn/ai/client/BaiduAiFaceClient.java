package com.ghgcn.ai.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.util.Base64Util;
import com.ghgcn.ai.AiFaceClient;

public class BaiduAiFaceClient implements AiFaceClient {

    String APP_ID = "10533739";
    String API_KEY = "DCkGcHtGlB8rxs24BTGbdBOQ";
    String SECRET_KEY = "TagluzL8GT4mmEAHwEjmvWxIL6hUiKxW";
    AipFace client = null;
    static String groupId = "sz";
    String imageType = "BASE64";

    public BaiduAiFaceClient() {
        client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
    }

    @Override
    public void addUser() {
        File faceDir = new File("C:\\Users\\yucan.zhang\\Pictures\\faces");
        String[] faceNames = faceDir.list();
        System.out.println("face photo count:" + faceNames.length);
        File[] faceFiles = faceDir.listFiles();
        Map<String,String> names = new HashMap<>();
        long s = System.nanoTime();
        HashMap<String,String> options = new HashMap<String,String>();

        options.put("quality_control", "NORMAL");// 图片质量控制 NONE: 不进行控制 LOW:较低的质量要求 NORMAL: 一般的质量要求 HIGH: 较高的质量要求 默认 NONE
        // options.put("liveness_control", "LOW");// 活体检测控制 NONE: 不进行控制 LOW:较低的活体要求(高通过率 低攻击拒绝率) NORMAL:
        // 一般的活体要求(平衡的攻击拒绝率,
        // 通过率) HIGH: 较高的活体要求(高攻击拒绝率 低通过率) 默认NONE

        String imageType = "BASE64";

        for (File file : faceFiles) {
            long s1 = System.nanoTime();
            String name = file.getName().substring(0, file.getName().lastIndexOf(".")).split("_")[0];
            String uid;
            if (names.containsKey(name)) {
                uid = names.get(name);
                System.out.println("add face to :" + name + ",uid=" + uid);
            } else {
                uid = UUID.randomUUID().toString().replace("-", "");
                System.out.println("add user :" + name + ",uid=" + uid);
            }
            String img;

            try {
                img = Base64Util.encode(Files.readAllBytes(file.toPath()));
                options.put("user_info", file.getName());
                JSONObject jobj = client.addUser(img, imageType, groupId, uid, options);
                int errorCode = jobj.getInt("error_code");
                if (errorCode == 0) {
                    names.put(name, uid);
                } else {
                    // qps超限,再执行一次
                    if (errorCode == 18) {
                        try {
                            Thread.sleep(400L);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                        jobj = client.addUser(img, imageType, groupId, uid, options);
                        errorCode = jobj.getInt("error_code");
                        if (errorCode == 0) {
                            names.put(name, uid);
                        }
                    }
                }
                System.out.println(jobj);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            long e1 = System.nanoTime() - s1;
            System.out.println("elapsed time:" + TimeUnit.NANOSECONDS.toMillis(e1));
        }
        long e = System.nanoTime() - s;
        System.out.println("total time:" + TimeUnit.NANOSECONDS.toSeconds(e) + " , okuser:" + names.size());
        // 每用户注册在290-1600ms,平均500ms左右,总耗时90-102s
    }

    @Override
    public void addGroup() {
    }

    @Override
    public void detect() {
        HashMap<String,String> options = new HashMap<>();
        options.put("face_field", "age,gender,glasses,facetype");
        options.put("max_face_num", "3");
        File faceImageFile = new File("C:\\Users\\yucan.zhang\\Pictures\\vague_faces");
        File[] files = faceImageFile.listFiles();
        for (File file : files) {
            String img = null;
            try {
                img = Base64Util.encode(Files.readAllBytes(file.toPath()));
                JSONObject jobj = client.detect(img, imageType, options);
                int errorCode = jobj.getInt("error_code");
                if (errorCode == 18) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    jobj = client.detect(img, imageType, null);
                }
                JSONObject dataNode = jobj.getJSONObject("result");
                JSONArray faceNodes = dataNode.getJSONArray("face_list");
                Iterator<Object> it = faceNodes.iterator();
                System.out.println(file.getName() + " detect: facenum=" + dataNode.getInt("face_num"));
                while (it.hasNext()) {
                    JSONObject node = (JSONObject) it.next();
                    String t = String.format("age:%s,probability=%s", node.has("age") ? node.getInt("age") : 0,
                            node.getDouble("face_probability"));
                    System.out.println(t);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
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

        // 2. 图片内容方式
        long s = System.nanoTime();
        File faceImageFile = new File("C:\\Users\\yucan.zhang\\Pictures\\compare_faces");
        File[] files = faceImageFile.listFiles();
        HashMap<String,String> options = new HashMap<String,String>();
        options.put("quality_control", "NORMAL");
        options.put("max_user_num", "3");
        for (File file : files) {
            long s1 = System.nanoTime();
            String img = null;
            try {
                img = Base64Util.encode(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jobj = client.search(img, imageType, groupId, options);

            int errorCode = jobj.getInt("error_code");
            System.out.println(String.format("photo:%s compare result:", file.getName()));
            if (errorCode == 0) {
                System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - s1));
                JSONObject dataNode = jobj.getJSONObject("result");
                JSONArray userNodes = dataNode.getJSONArray("user_list");
                Iterator<Object> it = userNodes.iterator();
                while (it.hasNext()) {
                    JSONObject obj = (JSONObject) it.next();
                    String out = String.format("===>%s 置信度:%s, pid:%s,fid:%s,", obj.getString("user_info"),
                            obj.getDouble("score"), obj.getString("user_id"), dataNode.getString("face_token"));
                    System.out.println(out);
                }
            } else if (errorCode == 18) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - s1));
                matchAndParse(imageType, options, s1, img, errorCode);
            } else {
                System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - s1));
                System.out.println(jobj);
            }
        }

        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - s));
        // 23张照片,人脸库151张,比对耗时:320-840ms每张,共12158ms
    }

    private void matchAndParse(String imageType, HashMap<String,String> options, long s1, String img, int errorCode) {
        JSONObject jobj;
        jobj = client.search(img, imageType, groupId, options);
        if (errorCode == 0) {
            System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - s1));
            JSONObject dataNode = jobj.getJSONObject("result");
            JSONArray userNodes = dataNode.getJSONArray("user_list");
            Iterator<Object> it = userNodes.iterator();
            while (it.hasNext()) {
                JSONObject obj = (JSONObject) it.next();
                String out = String.format("===>%s 置信度:%s, pid:%s,fid:%s,", obj.getString("user_info"),
                        obj.getDouble("score"), obj.getString("user_id"), dataNode.getString("face_token"));
                System.out.println(out);
            }
        }
    }

    @Override
    public void persionVerify() {
    }

    public void deleteUsersByGroupId(String groupId) {
        JSONObject ret = client.groupDelete(groupId, null);
        System.out.println(ret);
    }

    public static void main(String[] args) throws IOException {
        BaiduAiFaceClient client = new BaiduAiFaceClient();
        // client.addUser();
        // client.match();
        client.detect();
        // 删除用户
        // client.deleteUsersByGroupId(groupId);
    }
}
