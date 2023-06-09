package com.mmo.microservice.video.service.impl;

import com.microsoft.playwright.*;
import com.mmo.microservice.video.dto.DownloadPushDTO;
import com.mmo.microservice.video.dto.DownloadVideoDTO;
import com.mmo.microservice.video.dto.ReelRespDTO;
import com.mmo.microservice.video.service.DownloadService;
import com.mmo.microservice.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DownloadServiceImpl implements DownloadService {

    private final VideoService videoService;

    @Override
    public List<ReelRespDTO> downloadByID(DownloadPushDTO dto, String save_path, String tiktok_url, String snaptik_url){
        // TODO Auto-generated method stub
        List<ReelRespDTO> rResp = null;
        try {
//			DownloadTiktokService downloadServ = new DownloadTiktokService(appProp.APP_PROP_EXCHANGE_SAVE_PATH, appProp.APP_PROP_EXCHANGE_DRIVER_PATH, Constants.TIKTOK_URL, Constants.SNAPTIK_URL);
            List<DownloadVideoDTO> rs = downloadProcess(dto.getAcc_id(),dto.getPage_id(),save_path,tiktok_url,snaptik_url);
            rResp = videoService.saveLstVideoDownload(rs, dto.getPage_id()); //uploadBatchReels(rs, dto.getPage_id());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return rResp;
    }

    public List<DownloadVideoDTO> downloadProcess(String acc_id, Long p_id, String save_path, String tiktok_url, String snaptik_url) throws InterruptedException{
        List<DownloadVideoDTO> rs = new ArrayList<DownloadVideoDTO>();
        String parentFolderPath = save_path+ p_id;
        String childFolderName = acc_id;
        String folderDownload = save_path+ p_id+ File.separator+ acc_id;
        File folderDownloads = new File(folderDownload);
        if (!folderDownloads.exists()) {
            File parentFolder = new File(parentFolderPath);
            File newFolder = new File(parentFolder, childFolderName);
            boolean created = newFolder.mkdirs();
            if (!created) {
                System.out.println("Không thể tạo thư mục!");
                throw new IllegalArgumentException("Không thể tạo thư mục");
            }
            System.out.println("Thư mục tạo thành công!");
        }
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        Page page = browser.newPage();
        String url_download = tiktok_url+acc_id;
        page.navigate(url_download);
        Thread.sleep(10000L);
        this.autoScroll(page);
        List<ElementHandle> links = (List<ElementHandle>)page.querySelectorAll("div.tiktok-x6y88p-DivItemContainerV2.e19c29qe8 > div > div > div > a");
        List<ElementHandle> videoDes = (List<ElementHandle>)page.querySelectorAll("div.tiktok-1qb12g8-DivThreeColumnContainer.eegew6e2 > div > div > div > a");
        int count = 0;
        Map<String, String> urlMap = new HashMap<String, String>();
        List<String> mainArr = new ArrayList<String>();
        count = 0;
        for (ElementHandle link : links) {
            String href = link.getAttribute("href");
            String[] arrVid = href.split("/");
            String vid_id = arrVid[arrVid.length - 1];
            mainArr.add(vid_id);
            urlMap.put(vid_id, href);
            ++count;
        }
        List<String> arrSub = videoService.getVidByPageIDAndAccID((long)p_id, acc_id);
        List<String> downloadList = this.getDownloadList(mainArr, arrSub);
        System.out.println(downloadList.size());
        for (int i = 0; i < downloadList.size(); ++i) {
            page.navigate(snaptik_url);
            String urlVid = urlMap.get(downloadList.get(i));
            ElementHandle snapSearchBox = page.querySelector("input[name=\"url\"]");
            snapSearchBox.type(urlVid);
            String[] arrVid = urlVid.split("/");
            String vid_id = arrVid[arrVid.length - 1];
            ElementHandle button = page.querySelector(".button-go");
            button.click();
            ElementHandle downloadButton = page.waitForSelector(".video-links > a");
            ElementHandle titleDiv = page.querySelector(".video-title");
            String vidDes = titleDiv.innerText();
            String urlDecode = URLDecoder.decode(downloadButton.getAttribute("href"));
            try {
                URL url = new URL(urlDecode);
                HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
                int responseCode = httpConn.getResponseCode();
                if (responseCode == 200) {
                    String sourcePath = folderDownload + File.separator + vid_id + ".mp4";
                    InputStream inputStream = httpConn.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(sourcePath);
                    int bytesRead = -1;
                    byte[] buffer = new byte[4096];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                    rs.add(new DownloadVideoDTO(vid_id, vidDes, (String)null, sourcePath, acc_id));
                }
                else {
                    System.out.println("Failed to download video.");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        browser.close();
        playwright.close();
        System.out.println("Download successfully!");
        return rs;
    }

    public void autoScroll(Page page) throws InterruptedException {
        int totalHeight = 0;
        int distance = 100;
        while (true) {
            int scrollHeight = (int) page.evaluate("document.body.scrollHeight");
            page.evaluate("window.scrollBy(0, " + distance + ")");
            totalHeight += distance;
            if (totalHeight >= scrollHeight) {
                break;
            }
            Thread.sleep(100);
        }
//
    }

    public List<String> getDownloadList(List<String> arrMain, List<String> arrSub){
//		List<String> = new
//		int[] arr = {1,2,3,11,12,15,18,20,22,23};
////    	int[] rootArr = {1,2,3,4,5,6,7,8,9};
//    	List<Integer> mainArr = new ArrayList<>();
//    	for(int i = 1; i<1000000;i++) {
//    		mainArr.add(i);
//    	}
        int range = 30;
////    	List<Integer> arrLst =
////    	List<Integer> rootArrLst = ;
        long currTime = System.currentTimeMillis();
//    	List<String> subArr = Arrays.stream(arrSub).boxed().collect(Collectors.toList());
        System.out.println("subArr: "+arrSub);
//    	List<Integer> mainArr = Arrays.stream(rootArr).boxed().collect(Collectors.toList());
        System.out.println("mainArr: "+arrMain);
        arrMain.removeAll(arrSub);
        System.out.println("RemoveArr: "+arrMain);
        if(range>arrMain.size()) {
            range = arrMain.size();
        }
        List<String> newArr =  arrMain.subList(0, range);
        System.out.println("newArr: "+newArr);
        System.out.println("Time running: "+(System.currentTimeMillis()-currTime));
        return newArr;
    }
}
