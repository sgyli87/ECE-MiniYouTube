
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*; 
import java.lang.Thread;
import java.util.*;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.simple.*;
import org.json.simple.parser.*;
//import org.json.*;


class EchoHandler extends Thread{
    // extend Thread for handling all clients' connection
    Socket clientSocket;
    DatagramSocket udpSocket;
    String contentPath;
    String name;
    String uuid;
    String confFilePath;
    OutputStream output;
    InputStream input;
    String inputLine;
    static final int BUFFERSIZE = 1024;
    private static final int DATA_SIZE = 1024;
    EchoHandler (Socket clientSocket, OutputStream output, InputStream input ,DatagramSocket udpSocket, String uuid, String name, String contentPath,
                 String confFilePath,String inputLine){
        this.clientSocket=clientSocket;
        this.output = output;
        this.input = input;
        this.udpSocket = udpSocket;
        this.uuid = uuid;
        this.name = name;
        this.contentPath = contentPath;
        this.confFilePath = confFilePath;
        this.inputLine = inputLine;
    }

    public void run() {
        try{
            System.out.println ("Connection successful");
            System.out.println ("Waiting for input.....");
            //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true); 
            // OutputStream output = clientSocket.getOutputStream();
            // //PrintWriter out = new PrintWriter(output);
            // BufferedReader in = new BufferedReader( 
            //     new InputStreamReader( clientSocket.getInputStream())); 

            // String Range = null;
            //inputLine = in.readLine();
            // read the request input line
            //while ((inputLine = in.readLine()) != null) {
            //while (true) {
                String Range = null;
                //inputLine = in.readLine();
                System.out.println(inputLine);
                //System.out.println(inputLine.length());
                // if (inputLine.length() <= 0) {  
                //     break;  
                // } 
                String[] request = inputLine.split(" ");
                String method = request[0];
                String url = request[1]; 
                // System.out.println("start:");
                // System.out.println(method);
                // System.out.println(url);
                // System.out.println("End");
                String host = "";
                String port = "";
                String rate = "";
                String path = "";
                
                // Parse peer url
                if(url.startsWith("/peer/add")&&!url.startsWith("/peer/addneighbor")){
                    StringBuilder sb = new StringBuilder();
                    String[]  urlcomp = url.split("&");
                    String[] pathcomp = urlcomp[0].split("=");
                    //path = pathcomp[1];
                    //System.out.println(path);
                    sb.append(pathcomp[1]).append(",");
                    String[] hostcomp = urlcomp[1].split("=");
                    //host += hostcomp[1];
                    //System.out.println(host);
                    sb.append(hostcomp[1]).append(",");
                    String[] portcomp = urlcomp[2].split("=");
                    //port += portcomp[1];
                    //System.out.println(port);
                    sb.append(portcomp[1]).append(",");
                    if(urlcomp.length > 3){
                        String[] ratecomp = urlcomp[3].split("=");
                        //rate += ratecomp[1];
                        sb.append(ratecomp[1]);
                    }
                    else{
                        //rate += "0";
                        sb.append(0);
                    }
                    try{
                        File peers = new File("peers.txt");
                        FileOutputStream fos = null;
                        if(!peers.exists()){
                            peers.createNewFile();
                            fos = new FileOutputStream(peers);
                        }else{
                            fos = new FileOutputStream(peers, true);
                        }
                        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                        osw.write(sb.toString());
                        osw.write("\r\n");
                        osw.flush();
                        osw.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                // else if(url.startsWith("/peer/search")){
                //     System.out.println("initial a gossip search....");
                //     //InetAddress address = InetAddress.getByName(host);
                //     // byte[] syndata = "GSP".getBytes();
                //     // System.out.println(syndata.length);
                //     // //int syn_size = syndata.length();
                //     // byte[] firstdata = new byte[DATA_SIZE+3];
                //     // System.arraycopy(syndata, 0, firstdata, 0, 3);
                //     String contentKey = url.replace("/peer/search/", "");
                //     String TTL = "15";
                //     VodServer.contentsMap.getOrDefault(contentKey, "Initial");
                //     System.out.println(VodServer.contentsMap.get(contentKey));
                //     System.out.println("request content file: "+contentKey);
                //     Random random = new Random();
                //     int index = random.nextInt(VodServer.neighborsStr.size());
                //     String neighborStr = VodServer.neighborsStr.get(index);
                //     //for(String neighborStr :VodServer.neighborsStr) {
                //     String[] params = neighborStr.split("&");
                //     String uuid = params[0];
                //     String peerHost = params[1];
                //     String peerPort = params[3];
                //     InetAddress address = InetAddress.getByName(peerHost);
                //     //GSP:key:peers'uuid1(Null/Initial/split with &),peers'uuid2(Null/split with &):ttl   trim
                //     String gossipStr = "GSP:"+contentKey+":"+"Initial:"+TTL;
                //     byte[] gossipData = gossipStr.getBytes();
                //     byte[] firstdata = new byte[DATA_SIZE+3];

                //     // byte[] syndata = "GSP".getBytes();
                //     // System.out.println(syndata.length);
                //     // //int syn_size = syndata.length();
                //     //byte[] firstdata = new byte[DATA_SIZE+3];
                //     System.arraycopy(gossipData, 0, firstdata, 0, gossipData.length);
                    
                //     DatagramPacket firstPacket = new DatagramPacket(firstdata, firstdata.length, address, Integer.parseInt(peerPort));
                //     udpSocket.send(firstPacket);
                //     System.out.println("send Gossip package to "+uuid);
                //     //}



                    
                // }
                else if(url.startsWith("/peer/search")){
                    System.out.println("initial a gossip search....");
                    String contentKey = url.replace("/peer/search/", "");
                    //VodServer.requestList.add(contentKey);
                    Thread gossipThread = new Thread(new VodServer.GossipThread(contentKey,15,udpSocket));
                    gossipThread.start();
                    Thread.sleep(1700);
                    res200OK(new File("./json/search.json"), output);
                    
                }
                else if(url.startsWith("/peer/view")){
                    String targetContent = url.replace("/peer/view/", "");
                    if(VodServer.contentMap.containsKey(targetContent)){
                        String nowJson = VodServer.contentMap.toString();
                        JSONArray contentPeers = (JSONArray) VodServer.contentMap.get(targetContent);
                        String contentPeersStr = contentPeers.toJSONString();
                        // System.out.println(nowJson);
                        contentPeersStr = contentPeersStr.substring(1,contentPeersStr.length()-1);
                        String[] peersHaveContent = contentPeersStr.split(",");
                        JSONObject sdObject = new JSONObject();
                        try {
                            //String distanceInfo = new String(Files.readAllBytes(Paths.get("./json/shortestdistance.json")));
                            Reader reader = new FileReader("./json/shortestdistance.json");
                            JSONParser parser = new JSONParser();
                            JSONArray distanceInfo = (JSONArray) parser.parse(reader);
                            String distanceInfoStr = distanceInfo.toJSONString();
                            System.out.println(distanceInfoStr);
                            // distanceInfoStr = distanceInfoStr.substring(1,distanceInfoStr.length() - 1);
                            // distanceInfoStr = "{" + distanceInfoStr +"}";
                            
                            for (Object element : distanceInfo) {
                                JSONObject obj = (JSONObject) element;
                                for (Object entryObj : obj.entrySet()) {
                                    JSONObject.Entry entry = (JSONObject.Entry) entryObj;
                                    sdObject.put(entry.getKey(), entry.getValue());
                                }
                            }
                            // String sdObjectStr = sdObject.toJSONString();
                            // String httpResponse = "HTTP/1.1 200 OK\r\n" +
                            // "Content-Type: text/plain\r\n" +
                            // "Content-Length: " + sdObjectStr.length() + "\r\n" +
                            // "\r\n" +
                            // sdObjectStr;
                            // output.write(httpResponse.getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        long minMetric = Integer.MAX_VALUE;
                        String targetUUID = "";
                        String targetHost = "";
                        String targetBackendPort = "";
                        for(String peerStr: peersHaveContent){
                            peerStr = peerStr.substring(1, peerStr.length()-1);
                            String[] peerValue = peerStr.split("&");
                            String peerUUID = peerValue[0];
                            System.out.println("peers uuid = "+peerUUID);
                            String peerHost = peerValue[1];
                            String peerBackendPort = peerValue[2];
                            if(sdObject.containsKey(peerUUID)) {
                                long metric = (long) sdObject.get(peerUUID);
                                System.out.println(peerUUID+" 's metric = "+metric);
                                if(metric<=minMetric){
                                    targetUUID = peerUUID;
                                    targetHost = peerHost;
                                    targetBackendPort = peerBackendPort;
                                    minMetric = metric;
                                }
                            
                            } else {
                                continue;
                            }
                            


                        }
                        String minmsg = "min peers is "+targetUUID + "   port is " +targetBackendPort+ " metric is " +minMetric;
                        // String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        //                     "Content-Type: text/plain\r\n" +
                        //                     "Content-Length: " + minmsg.length() + "\r\n" +
                        //                     "\r\n" +
                        //                     minmsg;
                        // output.write(httpResponse.getBytes());
                        System.out.println(minmsg);
                        InetAddress address = InetAddress.getByName(targetHost);
                        byte[] syndata = "SYN".getBytes();
                        byte[] filenamehead = targetContent.getBytes();
                        System.out.println(syndata.length);
                        //int syn_size = syndata.length();
                        byte[] firstdata = new byte[DATA_SIZE+128];
                        System.arraycopy(syndata, 0, firstdata, 0, syndata.length);
                        System.arraycopy(filenamehead, 0, firstdata, 3, filenamehead.length);
                        
                
                        DatagramPacket firstPacket = new DatagramPacket(firstdata, firstdata.length, address, Integer.parseInt(targetBackendPort));
                        udpSocket.send(firstPacket);
                        
                        Thread.sleep(1500);
                        File file = new File("./content/"+targetContent);
                        res200OK(file, output);
                    } else {
                        String noContent = "You have not searched this content, or your peers don't have this content!";
                        System.out.println(noContent);

                        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                                            "Content-Type: text/plain\r\n" +
                                            "Content-Length: " + noContent.length() + "\r\n" +
                                            "\r\n" +
                                            noContent;
                        output.write(httpResponse.getBytes());
                    }
                    

                }

                else if(url.startsWith("/peer/kill")){
                    System.out.println("Process terminated by user.");
                    System.exit(0);
                }
                else if(url.startsWith("/peer/uuid")){
                    String jsonString = "{\"uuid\":" + "\""+uuid+"\"}";
                    BufferedWriter bw = new BufferedWriter(new FileWriter("./json/uuid.json"));
                    bw.write(jsonString);
                    bw.close();
                    res200OK(new File("./json/uuid.json"), output);
                }
                else if(url.startsWith("/peer/neighbors")){
                    res200OK(new File("./json/neighbors.json"),output);
                }
                else if(url.startsWith("/peer/addneighbor")){
                    String peerinfo = url.replace("/peer/addneighbor?","");

                    System.out.println("peerInfo:" +peerinfo);
                    File confFile = new File(confFilePath);
                    try{
                        FileReader reader = new FileReader(confFile);
                        Properties props = new Properties();
                        props.load(reader);

                        int peerNumber = Integer.parseInt(props.getProperty("peer_count"));
                        peerNumber++;
                        props.put("peer_"+ (peerNumber-1), peerinfo);
                        // read the last peer number and add one new line
                        props.setProperty("peer_count", String.valueOf(peerNumber));

                        FileWriter writer = new FileWriter(confFile);

                        props.store(writer, "Add new peer");
                        System.out.println("add new peer");
                        writer.close();
                    }
                    catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                else if(url.startsWith("/peer/map")){
                    res200OK(new File("./json/map.json"),output);
                }
                else if(url.startsWith("/peer/rank")) {
                    // get own UID
                    StringBuffer mapJsonBuffer = new StringBuffer();
                    File mapJsonFile = new File("./json/map.json");
                    if (!mapJsonFile.exists()) {
                        System.err.println("Can't Find " + mapJsonFile);
                    }
                    try {
                        FileInputStream fis = new FileInputStream(mapJsonFile);
                        InputStreamReader inputStreamReader = new InputStreamReader(fis, "UTF-8");
                        BufferedReader inrank  = new BufferedReader(inputStreamReader);
                        
                        String str;
                        while ((str = inrank.readLine()) != null) {
                            mapJsonBuffer.append(str.trim());  //new String(str,"UTF-8")
                        }
                        inrank.close();
                    } catch (IOException e) {
                        e.getStackTrace();
                    }
                    String jsonData = mapJsonBuffer.toString();

                    
                    List<String> neighborString = new ArrayList<>();
                    // Parse JSON data and add edges to graph
                    Map<String, Map<String, Integer>> dataMap = new HashMap<>();
                    Map<String, Object> parsedData = new HashMap<>();
                    String subJsonData = jsonData.substring(1,jsonData.length() - 1);
                    System.out.println("subJsonData: "+subJsonData);
                    String[] pairs = subJsonData.split("}");
                    for (int i = 0;i<pairs.length;i++) {
                        pairs[i] = pairs[i] + "}";
                        if(pairs[i].charAt(0) == ','){
                            pairs[i] = pairs[i].substring(1);
                        }
                    }
                    for (String pair : pairs) {
                        nodeJsonParse(pair,dataMap);
                    }

                    Map<String, Integer> distances = shortestDistances(dataMap, uuid);
                    StringBuffer rankString = new StringBuffer();
                    List<Map.Entry<String, Integer>> sortedDistances = new ArrayList<>(distances.entrySet());
                    Collections.sort(sortedDistances, Map.Entry.comparingByValue());
                    for (Map.Entry<String, Integer> entry : sortedDistances) {
                        String node = entry.getKey();
                        int distance = entry.getValue();
                        System.out.println("Shortest path from node4 to " + node + ": " + distance);
                        if(distance>0) rankString.append("{\""+node+"\":"+String.valueOf(distance)+"},");
                    }
                    //jsonString.append(entry);

                    rankString.insert(0,"[");
                    String writeRankString = rankString.toString();
                    writeRankString = writeRankString.substring(0,writeRankString.length() - 1) + "]";
                    BufferedWriter bw = new BufferedWriter(new FileWriter("./json/shortestdistance.json"));
                    bw.write(writeRankString);
                    bw.close();

                    res200OK(new File("./json/shortestdistance.json"),output);
                }


            clientSocket.close();
        }
        catch (Exception e){
            System.err.println(e);
            //res500InternalServerError();
            e.getStackTrace();
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            String error = stringWriter.toString();
            //System.out.println(error);
            if(!error.startsWith("java.net.SocketException")){
             //&& !error.startsWith("java.net.SocketException: Connection reset by peer: socket write error")){
                System.err.println("Exception caught: Client Disconnected.");
            }
            
        }
        finally{
            try{
                clientSocket.close();
            }
            catch(Exception e){;}
        }
    }
    private static void res404NotFound(OutputStream output) throws IOException {
        // send 404 response for not found file request
        PrintWriter out = new PrintWriter(output);
        final String CRLF = "\r\n";
        String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern,Locale.ENGLISH);
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(timeZone);
        Date nowDate = new Date(); 
        String nowDateGMT = sdf.format(nowDate);
        System.out.println(nowDateGMT);
        
        String response = "HTTP/1.1 404 Not Found" + CRLF +
                "Date: " + nowDateGMT + CRLF +
                "Connection: " + "Keep-Alive" + CRLF +
                CRLF;
        out.println(response);
        output.write(response.getBytes("UTF-8"));
        out.flush();
    }
    private static void res405MethodNotAllowed(OutputStream output) throws IOException {
        // send 405 response for unexpected request methods
        PrintWriter out= new PrintWriter(output);
        final String CRLF = "\r\n";
        String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern,Locale.ENGLISH);
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(timeZone);
        Date nowDate = new Date(); 
        String nowDateGMT = sdf.format(nowDate);
        System.out.println(nowDateGMT);
        String response = "HTTP/1.1 405 Method Not Allowed" + CRLF +
                "Date: " + nowDateGMT + CRLF +
                "Connection: " + "Keep-Alive" + CRLF +
                CRLF;
        out.println(response);
        output.write(response.getBytes("UTF-8"));
        out.flush();
    }

    private static void res500InternalServerError(OutputStream output) throws IOException {
        // send 500 response for server error
        PrintWriter out = new PrintWriter(output);
        final String CRLF = "\r\n";
        
        String response = "HTTP/1.1 500 Internal Server Error" + CRLF +
                "Content-Type: text/plain" + CRLF +
                "Content-Length: 19" + CRLF +
                CRLF;
        out.println(response);
        output.write(response.getBytes("UTF-8"));
        out.flush();
    }
    public static void nodeJsonParse(String nodeJson,Map<String, Map<String, Integer>> dataMap) {
        String[] parts = nodeJson.split("\\:\\{");
        parts[0] = parts[0].substring(1,parts[0].length() - 1);
        parts[1] = parts[1].substring(0,parts[1].length() - 1);
        //System.out.println(parts[0]);
        //System.out.println(parts[1]);
        Map<String, Integer> innerMap = new HashMap<>();
        //return parts;
        String[] distances = parts[1].split(",");
        for (String distance : distances) {
            String[] pairParts = distance.split(":");
            String innerKey = pairParts[0].substring(1,pairParts[0].length() - 1);
            //System.out.println("innerKey: "+innerKey);
            int innerValue = Integer.parseInt(pairParts[1]);
            innerMap.put(innerKey, innerValue);
        }
        dataMap.put(parts[0], innerMap);
    }
    public static Map<String, Integer> shortestDistances(Map<String, Map<String, Integer>> graph, String source) {
        // Initialize distances to all nodes as infinity, except for the source node
        Map<String, Integer> distances = new HashMap<>();
        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(source, 0);

        // Initialize priority queue with source node
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparing(distances::get));
        queue.add(source);

        // Process nodes in priority queue, updating distances as necessary
        while (!queue.isEmpty()) {
            String node = queue.poll();
            int distance = distances.get(node);

            // Update distances of neighbors
            Map<String, Integer> neighbors = graph.get(node);
            for (String neighbor : neighbors.keySet()) {
                int newDistance = distance + neighbors.get(neighbor);
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    queue.add(neighbor);
                }
            }
        }

        return distances;
    }
    private static void res200OK(File file, OutputStream output) throws IOException {
        // send 200 response with GMT Zone formatted time
        String contentType = getContentType(file);
        PrintWriter out = new PrintWriter(output);
        final String CRLF = "\r\n";
        String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern,Locale.ENGLISH);
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(timeZone);
        Date nowDate = new Date(); 
        String nowDateGMT = sdf.format(nowDate);
        System.out.println(nowDateGMT);
        
        String response = "HTTP/1.1 200 OK" + CRLF +
                "Date: " + nowDateGMT + CRLF +
                "Connection: " + "Keep-Alive" + CRLF +
                "Content-Length: " + file.length() + CRLF +
                "Content-Type: " + contentType + CRLF +
                "Last-Modified" + LastModifiedDate(file) + CRLF +
                CRLF;
        out.println(response);
        output.write(response.getBytes("UTF-8"));
        // send the whole file
        FileInputStream fileStream = new FileInputStream(file);
        byte[] buffer = new byte[BUFFERSIZE];
        int part;
        while ((part = fileStream.read(buffer)) >= 0) {
            output.write(buffer, 0, part);
        }
        fileStream.close();
        out.flush();
    }
    private static void res206PartialContent(File file, OutputStream output, String Range) throws IOException {
        // split the range and get the left and right value, and send 206 response
        String contentType = getContentType(file);
        String[] partContent = Range.split("=")[1].split("-");
        int length = partContent.length;
        long left;
        long right;
        System.out.println(partContent.length);
        final String CRLF = "\r\n";
        String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern,Locale.ENGLISH);
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(timeZone);
        Date nowDate = new Date(); 
        String nowDateGMT = sdf.format(nowDate);
        System.out.println(nowDateGMT);
        String response;
        PrintWriter out = new PrintWriter(output);
        if(length == 1){
            left = Long.parseLong(partContent[0]);
            right = file.length() - 1;
            long contentLength = right - left + 1;
            response = "HTTP/1.1 206 Partial Content" + CRLF +
            "Date: " + nowDateGMT + CRLF +
            "Connection: " + "Keep-Alive" + CRLF +
            "Content-Length: " + contentLength + CRLF +
            "Content-Range: bytes " + left + "-" + right  + "/" +file.length() + CRLF + 
            "Content-Type: " + contentType + CRLF +
            "Last-Modified" + LastModifiedDate(file) + CRLF +
            CRLF;
        }else {
            left = Long.parseLong(partContent[0]);
            right = Long.parseLong(partContent[1]);
            long contentLength = right - left + 1;
            response = "HTTP/1.1 206 Partial Content" + CRLF +
            "Date: " + nowDateGMT + CRLF +
            "Connection: " + "Keep-Alive" + CRLF +
            "Content-Length: " + contentLength + CRLF +
            "Content-Range: bytes " + left + "-" + right + "/" +file.length() + CRLF + 
            "Content-Type: " + contentType + CRLF +
            "Last-Modified" + LastModifiedDate(file) + CRLF +
            CRLF;
        }
        out.println(response);
        output.write(response.getBytes("UTF-8"));
        // send the partial content based on range request
        FileInputStream fileInput = new FileInputStream(file);
        fileInput.skip(left);
        byte[] buffer = new byte[BUFFERSIZE];
        int part;
        while ((part = fileInput.read(buffer)) >= 0 && left <= right) {
            output.write(buffer, 0, part);
            left += part;
        }
        fileInput.close();
        out.flush();
    }

    private static String getContentType(File file) {
        // check file type
        String content = file.getName();
        if (content.endsWith(".txt")) {
            return "text/plain";
        } else if (content.endsWith(".css")) {
            return "text/css";
        } else if (content.endsWith(".htm") || content.endsWith(".html")) {
            return "text/html";
        } else if (content.endsWith(".gif")) {
            return "image/gif";
        } else if (content.endsWith(".jpg") || content.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (content.endsWith(".png")) {
            return "image/png";
        } else if (content.endsWith(".js")) {
            return "application/javascript";
        } else if (content.endsWith(".json")){
            return "application/json";
        } else if (content.endsWith(".mp4") || content.endsWith(".webm") || content.endsWith(".ogg")) {
            return "video/webm";
        } else {
            return "application/octet-stream";
        }
    }

    private static String LastModifiedDate(File file) {
        // standard GMT Zone lastest modified date format
        String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern,Locale.ENGLISH);
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(timeZone);
        Long lastModifiedDate = file.lastModified();
        return sdf.format(lastModifiedDate);
    }

}

public class VodServer { 
    // private DatagramSocket udpSocket;
    // private int udpPort;
    // private InetAddress address;
    private static final int DATA_SIZE = 1024;
    public static final int search_interval = 100;
    public static HashMap<String,String> contentsMap = new HashMap<>();
    public static List<String> neighborsStr = new LinkedList<>();
    public static List<String> requestList = new LinkedList<>();
    public static JSONObject contentMap = new JSONObject();
    
    public static String uuid = "";
    public static String name = "";
    public static String contentPath = "";
    public static int peerNumber = 0;
    public static int udpPort = 0;
    public static int PortNumber = 0;
    // public VodServer(int udpPort, InetAddress address) throws IOException {
    //     this.udpPort = udpPort;
    //     this.address = address;
    //     this.udpSocket = new DatagramSocket(udpPort);
    // }
    public static class GossipThread implements Runnable {
        private int TTL;
        private String contentKey;
        private DatagramSocket udpSocket;

        public GossipThread(String contentKey, int TTL,DatagramSocket udpSocket) {
            this.contentKey = contentKey;
            this.TTL=TTL;
            this.udpSocket = udpSocket;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
            System.out.println("Gossip Thread start, TTL = "+TTL);
            while(true) {
                if(TTL==0) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter("./json/search.json"));
                    JSONArray gossipJson = (JSONArray) contentMap.getOrDefault(contentKey, new JSONArray());
                    // bw.write(gossipJson.toJSONString());
                    // bw.close();

                    JSONObject searchJSON = new JSONObject();

                    searchJSON.put("peers", gossipJson);
                    searchJSON.put("content", contentKey);

                    bw.write(searchJSON.toString());
                    bw.close();
                    break;   
                }
                long start = System.currentTimeMillis();
                String filePath = "./content/"+contentKey; 
                File contentFile = new File(filePath);
                JSONArray gossipJson = (JSONArray) contentMap.getOrDefault(contentKey, new JSONArray());
                if (contentFile.exists()) {
                    //tostring and compare
                    String testStr = gossipJson.toJSONString();
                    System.out.println(testStr);
                    if(!testStr.contains(uuid)){
                        gossipJson.add(uuid+"&"+"localhost"+"&"+udpPort);
                    } else {
                        System.out.println("already save it");
                    }
                    System.out.println("File exists.");
                } 
                Random random = new Random();
                int index = random.nextInt(neighborsStr.size());
                String neighborStr = neighborsStr.get(index);

                //for(String neighborStr :VodServer.neighborsStr) {
                String[] params = neighborStr.split("&");
                String peeruuid = params[0];
                String peerHost = params[1];
                String peerPort = params[3];
                InetAddress address = InetAddress.getByName(peerHost);

                //GSP:key:peers'uuid1(Null/Initial/split with &),peers'uuid2(Null/split with &):ttl   trim
                String gossipStr = "GSP:"+contentKey+":"+Integer.toString(TTL);
                String gossipJsonStr = gossipJson.toJSONString();
                byte[] gossipData = gossipStr.getBytes();
                byte[] contentData = gossipJsonStr.getBytes();
                
                byte[] firstdata = new byte[DATA_SIZE+3];
                System.arraycopy(gossipData, 0, firstdata, 0, gossipData.length);
                System.arraycopy(contentData, 0, firstdata, 128, contentData.length);
                // byte[] syndata = "GSP".getBytes();
                // System.out.println(syndata.length);
                // //int syn_size = syndata.length();
                //byte[] firstdata = new byte[DATA_SIZE+3];
                
                DatagramPacket firstPacket = new DatagramPacket(firstdata, firstdata.length, address, Integer.parseInt(peerPort));
                try {
                    udpSocket.send(firstPacket);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("send Gossip package to "+peeruuid);
                TTL--;
                long end = System.currentTimeMillis();
                Thread.sleep(Math.max(1, search_interval - (end - start)));
            
            }
        }catch (UnknownHostException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
            
        }
    }
    public static class SenderThread implements Runnable {
        private DatagramSocket udpSocket;
        private InetAddress address;
        private int port;
        private String filename;

        public SenderThread(DatagramSocket udpSocket, InetAddress address, int port,String filename) {
            this.udpSocket = udpSocket;
            this.address = address;
            this.port = port;
            this.filename = filename;
        }

        @Override
        public void run() {
            try {
                // Wait for SYN
                InetAddress urlAdd = InetAddress.getByName("localhost"); // a ip string for input of the class
                int urlPort = 7077; //distination
                // byte[] synData = new byte[1024];
                // DatagramPacket synPacket = new DatagramPacket(synData, synData.length);
                // System.out.println("Wait for SYN");
                // socket.receive(synPacket);
                // String syn = new String(synPacket.getData(), 0, synPacket.getLength());
                // System.out.println("F get syn: "+syn);
                // if (!syn.equals("SYN")) {
                //     System.out.println("SYN received");
                // }
               
                // Send the file
                System.out.println("waiting for file transfer request");
                String filepath = "./content/" + filename;
                File file = new File(filepath);
                byte[] buffer = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(buffer, 0, buffer.length);
                fis.close();

                int packetSize = 1024;
                int numberOfPackets = (int) Math.ceil((double) buffer.length / packetSize);
                System.out.println("chunk numbers:");
                System.out.println(numberOfPackets);
                int lastPacketSize = buffer.length % packetSize;
                if (lastPacketSize == 0) {
                    lastPacketSize = packetSize;
                }

                byte[] data = new byte[packetSize];
                for (int i = 0; i < numberOfPackets; i++) {
                    if (i == numberOfPackets - 1) {
                        data = Arrays.copyOfRange(buffer, i * packetSize, i * packetSize + lastPacketSize);
                    } else {
                        data = Arrays.copyOfRange(buffer, i * packetSize, (i + 1) * packetSize);
                    }
                    byte[] tagDAT = "DAT".getBytes();
                        
                    byte[] filenameheader = filename.getBytes();

                    byte[] DATfiledata = new byte[packetSize];
                    System.arraycopy(tagDAT, 0, DATfiledata, 0, 3);
                    System.arraycopy(filenameheader, 0, DATfiledata, 3, filenameheader.length);
                    System.arraycopy(data, 0, DATfiledata, 128, data.length);
                    DatagramPacket packet = new DatagramPacket(DATfiledata, data.length+128, address, port);
                    udpSocket.send(packet);

                    // // Wait for ACK
                    // byte[] receiveData = new byte[DATA_SIZE+3];
                    // DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    // System.out.println("Wait for ACK");
                    // socket.receive(receivePacket);
                    // //System.out.println("ACK received");

                    // String ack = new String(receivePacket.getData(), 0, 3);
                    // System.out.println(ack);
                    // if (!ack.equals("ACK")) {
                    //     System.err.println("Error: ACK not received.");
                    //     break;
                    // }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ReceiverThread implements Runnable {
        private DatagramSocket udpSocket;
        private String confFilePath;
        public ReceiverThread(DatagramSocket udpSocket, String confFilePath) {
            this.udpSocket = udpSocket;
            this.confFilePath = confFilePath;
        }
        // public ReceiverThread(DatagramSocket udpSocket,String confFilePath, Socket clienSocket,OutputStream output) {
        //     this.udpSocket = udpSocket;
        //     this.confFilePath = confFilePath;
        //     // this.clientSocket = clienSocket;
        //     // this.output = output;
        // }
        private HashMap<String, Date> activePeers = new HashMap<>();
        private HashMap<String, Integer> seqReceived = new HashMap<>();
        private HashMap<String, HashSet<String>> adjlist = new HashMap<>();
        private Date lastUID = new Date();
        private boolean gosFlag = false;

        @Override
        public void run() {
            try {
                System.out.println("waiting for URL parsing result");
                //InetAddress urlAdd = InetAddress.getByName("localhost"); // a ip string for input of the class
                //int urlPort = 7077; //destination
                // // Send SYN
                // byte[] syndata = "SYN".getBytes();
                // DatagramPacket synPacket = new DatagramPacket(syndata, syndata.length, urlAdd, urlPort);
                // socket.send(synPacket);
                boolean initial = true;
                //Thread.sleep(5000);
                Thread checkUID = new Thread(() -> {
                    while(true){
                        try{    
                            System.out.println("new check");
                        
                            if(Math.abs(new Date().getTime() - lastUID.getTime()) > 60000){
                                BufferedWriter bw = new BufferedWriter(new FileWriter("./json/neighbors.json"));
                                bw.write("{\"active\":[]}");
                                bw.close();
                            }
                            Thread.sleep(30000);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                checkUID.start();

                while (true) {
                    // Receive the file
                    byte[] buffer = new byte[DATA_SIZE+3];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    //System.out.println("waiting for data packet");
                    udpSocket.receive(packet);
                    System.out.println(packet.getAddress());
                    System.out.println(packet.getPort());
                    String tag = new String(packet.getData(), 0, 3);
                    System.out.println("receiver tag: "+tag);
                    System.out.println(initial+" <-initial");
                    System.out.println(new Date().getTime() - lastUID.getTime());
                    // if(!initial && Math.abs(new Date().getTime() - lastUID.getTime()) > 60000){
                    //     BufferedWriter bw = new BufferedWriter(new FileWriter("./json/neighbors.json"));
                    //     bw.write("[]");
                    //     bw.close();
                    // }


                    if (tag.equals("SYN")) {
                        System.out.println("start a sender thread");
                        String filename = new String(packet.getData(),3,128); //10
                        filename = filename.trim();
                        Thread newsender = new Thread(new SenderThread(udpSocket, packet.getAddress(), packet.getPort(),filename));
                        newsender.start();
                    } else if(tag.equals("ACK")) {
                        System.out.println("GOT ack");
                    } else if (tag.equals("UID")){
                        System.out.println("GOT uuid of other alive neighbors");
                        initial = false;
                        // create a global variable in Receiver thread Hash Map <String uuid, Boolean timeout> 
                        // to determine if we should delete the node
                        // write each time of receiving uuid to configfile and record the time, caculate the difference of times (>60s timeout)
                        String peerUUID = new String(packet.getData(), 3, 36);
                        Date currentDate = new Date();
                        lastUID = currentDate;

                        activePeers.put(peerUUID, currentDate);
                        StringBuilder jsonString = new StringBuilder();
                        String jsonPath = "./json/neighbors.json";

                        File confFile = new File(confFilePath);
                        HashMap<String, String> peers = new HashMap<>();

                        try{
                            FileReader reader = new FileReader(confFile);
                            Properties props = new Properties();
                            props.load(reader);

                            int peerNumber = Integer.parseInt(props.getProperty("peer_count"));
                            for(int i = 0; i < peerNumber; i++){
                                String info = props.getProperty("peer_" + i);
                                String uuidKey = info.split("&")[0];
                                peers.put(uuidKey.substring(5, 41), info);
                                System.out.println(uuidKey);
                            }

                            //form json file entries
                            for(String key: activePeers.keySet()){
                                System.out.println("key: "+key);
                                long diff = Math.abs(currentDate.getTime() - activePeers.get(key).getTime());
                                // if smaller than timeout
                                if( diff <= 60000 ){
                                    String activeInfo = peers.get(key);
                                    System.out.println(activeInfo);
                                    String[] infoComp = activeInfo.split("&");

                                    String entry = "{\"uuid\":" + "\"" + infoComp[0].split("=")[1] + "\","
                                            + "\"host\":" + "\"" + infoComp[1].split("=")[1] + "\","
                                            + "\"frontend\":" +  infoComp[2].split("=")[1] + ","
                                            + "\"backend\":" + infoComp[3].split("=")[1] + ","
                                            + "\"metric\":"  + infoComp[4].split("=")[1] + "},";

                                    jsonString.append(entry);
                                }
                                else{
                                    //delete dead neighbors from adjlist
                                    adjlist.remove(key);
                                }
                            }

                            jsonString.deleteCharAt(jsonString.length()-1);
                            jsonString.insert(0,"[").append("]");
                            jsonString.insert(0,"{\"active\":").append("}");
                            BufferedWriter bw = new BufferedWriter(new FileWriter(jsonPath));
                            bw.write(jsonString.toString());
                            bw.close();

                        }catch(FileNotFoundException e){
                            e.printStackTrace();
                        }catch(IOException e){
                            //
                        }
                    }
                    else if (tag.equals("LSA")){
                        System.out.println("Link state advertisement received:");
                        String linkState = new String(packet.getData());
                        System.out.println("link state is: " + linkState);

                        String[] linkStateComp = linkState.split(";");

                        String neighborNameFrom = linkStateComp[1];
                        String mapReceived = linkStateComp[2];
                        int seqNumReceived = Integer.parseInt(linkStateComp[3]);

                        System.out.println(neighborNameFrom);
                        System.out.println(mapReceived);
                        System.out.println(seqNumReceived);

                        mapReceived = mapReceived.substring(1, mapReceived.length()-1);
                        mapReceived = mapReceived + ",";

                        String[] breakDown = mapReceived.split("},");
                        for(int i = 0; i < breakDown.length; i++) breakDown[i] = breakDown[i].replace(":{", "&");

                        HashMap<String,HashSet<String>> tempMap = new HashMap<>();

                        for(String s: breakDown){
                            System.out.println(s);
                            String[] edges = s.split("&");

                            String nodeName = edges[0].substring(1,edges[0].length()-1);
                            String[] neighbors = edges[1].split(",");

                            for(int i = 0; i < neighbors.length; i++){
                                neighbors[i] = neighbors[i].substring(1);
                                neighbors[i] = neighbors[i].replace("\":",",");
                            }

                            if(!tempMap.containsKey(nodeName)){
                                tempMap.put(nodeName, new HashSet<>());
                                for(String n: neighbors) tempMap.get(nodeName).add(n);
                            }
                            else{ for(String n: neighbors) tempMap.get(nodeName).add(n);}
                        }

                        boolean discard = true;

                        if(!seqReceived.containsKey(neighborNameFrom) || seqReceived.get(neighborNameFrom) < seqNumReceived){
                            discard = false;
                            seqReceived.put(neighborNameFrom, seqNumReceived);
                            //update
                            for(String key: tempMap.keySet()){
                                if(adjlist.containsKey(key)){
                                    for(String s: tempMap.get(key)){
                                        adjlist.get(key).add(s);
                                    }
                                }
                                else{
                                    adjlist.put(key, new HashSet<>());
                                    for(String s: tempMap.get(key)){
                                        adjlist.get(key).add(s);
                                    }
                                }
                            }
                        }
                        else{
                            discard = true;
                        }

                        if(!discard){
                            StringBuilder jsonString = new StringBuilder();

                            for(String key: adjlist.keySet()){
                                jsonString.append("\""+key+"\":{");
                                for(String record: adjlist.get(key)){
                                    String name = record.split(",")[0];
                                    String metric = record.split(",")[1];
                                    jsonString.append("\""+name+"\":").append(metric+",");
                                }
                                jsonString.deleteCharAt(jsonString.length()-1);
                                jsonString.append("},");
                            }

                            jsonString.deleteCharAt(jsonString.length()-1);
                            jsonString.insert(0,"{").append("}");

                            try{
                                BufferedWriter bw = new BufferedWriter(new FileWriter("./json/map.json"));
                                bw.write(jsonString.toString());
                                bw.close();
                            }catch (FileNotFoundException e){
                                e.printStackTrace();
                            }

                            //adjlist.clear();
                        }
                    }
                    else if (tag.equals("GSP")){
                        String gossipStr = new String(packet.getData(), 0, 128);
                        gossipStr = gossipStr.trim();
                        System.out.println("gossipString: "+gossipStr);
                        String[] params = gossipStr.split(":");
                        String newKey = params[1];

                        String TTL = params[2];
                        //update
                        int newTTL = Integer.parseInt(TTL) - 1;
                        String jsonStr = new String(packet.getData(), 128, packet.getLength()-128);
                        jsonStr = jsonStr.trim();
                        JSONArray nodeContentMap = (JSONArray) contentMap.getOrDefault(newKey, new JSONArray());
                        if(jsonStr.length()>10){
                            JSONArray gosJsonArr = new JSONArray();
                            jsonStr = jsonStr.substring(1,jsonStr.length() -1);
                            System.out.println("jsonStr: "+jsonStr);
                            String[] peerJsons = jsonStr.split(",");
                            for(String peerJson : peerJsons) {
                                gosJsonArr.add(peerJson.substring(1, peerJson.length()-1));
                            }
                            System.out.println(gosJsonArr.toJSONString());
                            
                            for(Object gosJsonInfo : gosJsonArr){
                                if(!nodeContentMap.contains(gosJsonInfo)){
                                    nodeContentMap.add(gosJsonInfo);
                                }
                            }
                            
                        }
                        String newStr = nodeContentMap.toJSONString();
                        String filePath = "./content/"+newKey; 
                        File contentFile = new File(filePath);
                        
                        if(contentFile.exists() && !newStr.contains(uuid)){
                            nodeContentMap.add(uuid+"&"+"localhost"+"&"+udpPort);
                        }
                        contentMap.put(newKey, nodeContentMap);
                        // if(newTTL == 0){
                        //     BufferedWriter bw = new BufferedWriter(new FileWriter("./json/search.json"));
                        //     bw.write(nodeContentMap.toJSONString());
                        //     bw.close();
                        // }
                        if(gosFlag==false) {
                            Thread gossipThread = new Thread(new GossipThread(newKey,newTTL,udpSocket));
                            gossipThread.start();
                            gosFlag = true;
                        }
                        if(newTTL==0){
                            gosFlag = false;
                        }
                    }

                    //else if (tag.equals("srt") ) {
                    else if (tag.equals("DAT")){
                        String filename = new String(packet.getData(), 3, 125);
                        filename = filename.trim();
                        // String datStr = new String(packet.getData(), 128, 1024);
                        // datStr = datStr.trim();
                        // File newFile = new File("./content/"+filename);
                        // newFile.createNewFile();
                        //FileOutputStream fos = new FileOutputStream(new File(".\\content\\"+filename));
                        String filepath = "./content/"+filename;
                        System.out.println("filename = " +filename+"  ahahah");
                        // String filepath1 = "./content/1.txt";
                        // if(filepath.equals(filepath1)) System.out.println("equal path!!!!!!!!!!!!!!!!!");
                        FileOutputStream fos = new FileOutputStream(filepath);
                        fos.write(packet.getData(), 128, packet.getLength()-128);
                        // String file = new String(packet.getData(),3,32); //10
                        // file = file.trim();
                        byte[] ack = "ACK".getBytes();
                        byte[] ackdata = new byte[DATA_SIZE+3];
                        System.arraycopy(ack, 0, ackdata, 0, 3);
                        DatagramPacket ackPacket = new DatagramPacket(ackdata, ackdata.length, packet.getAddress(), packet.getPort());
                        udpSocket.send(ackPacket);
                        fos.close();
                        // File fileplay = new File("./content/"+file);
                        // res200OK(fileplay,output);
                    }

                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }
    public static class KeepAliveThread implements Runnable {
        private DatagramSocket udpSocket;
        private String confFilePath;
        public KeepAliveThread(DatagramSocket udpSocket, String confFilePath) {
            this.udpSocket = udpSocket;
            this.confFilePath = confFilePath;
        }
        @Override
        public void run() {
            try {
                System.out.println("Start sending keep alive message to neighbors");
                //InetAddress urlAdd = InetAddress.getByName("localhost"); // a ip string for input of the class
                //int urlPort = 7077; //distination
                // // Send SYN
                // byte[] syndata = "SYN".getBytes();
                // DatagramPacket synPacket = new DatagramPacket(syndata, syndata.length, urlAdd, urlPort);
                // socket.send(synPacket);
                int sendtime = 1;
                while (true) {
                    System.out.println("keep alive message send round: "+sendtime);

                    File confFile = new File(confFilePath);
                    FileReader reader = new FileReader(confFile);
                    Properties props = new Properties();
                    props.load(reader);
                    int peerNumber = Integer.parseInt(props.getProperty("peer_count"));
                    
                    for(int i = 0; i < peerNumber; i++){
                        String peerInstance = props.getProperty("peer_"+ i);
                        
                        String uuid = props.getProperty("uuid");
                        System.out.println("dir uuid:"+uuid);

                        byte[] uidTag = "UID".getBytes();

                        byte[] uuidData = uuid.getBytes();
                        System.out.println(uuidData.length);
                        //int syn_size = syndata.length();
                        byte[] uuidContent = new byte[DATA_SIZE+3];
                        System.arraycopy(uidTag, 0, uuidContent, 0, 3);
                        System.arraycopy(uuidData, 0, uuidContent, 3, 36);
                        //frontend
                        int start1 = peerInstance.indexOf("host=") + "host=".length();
                        int end1 = peerInstance.indexOf("&frontend");
                        String desAdd = peerInstance.substring(start1, end1);
                        //backend
                        int start2 = peerInstance.indexOf("backend=") + "backend=".length();
                        int end2 = peerInstance.indexOf("&metric");
                        String desPort = peerInstance.substring(start2, end2);

                        DatagramPacket uuidPacket = new DatagramPacket(uuidContent, uuidContent.length, InetAddress.getByName(desAdd), Integer.parseInt(desPort));
                        udpSocket.send(uuidPacket);
                        System.out.println("uuid sent to peer_"+i+"  Destination ip: "+desAdd+"  Destination port:"+desPort);

                    }
                    reader.close();
                    Thread.sleep(5000);
                    sendtime++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e){
            }
        }
    }
    public static class neighborMapCalculate implements Runnable {
        private String uuid;
        public neighborMapCalculate(String uuid) {
            this.uuid = uuid;
        }
        @Override
        public void run()  {
            System.out.println("in neighbor cacluater");
            while(true){
            try{
                StringBuffer strBuffer = new StringBuffer();
                String jsonPath = "./json/neighbors.json";
                FileInputStream fis = new FileInputStream(jsonPath);
                InputStreamReader inputStreamReader = new InputStreamReader(fis, "UTF-8");
                BufferedReader in  = new BufferedReader(inputStreamReader);
                
                String str;
                while ((str = in.readLine()) != null) {
                    strBuffer.append(str.trim());  //new String(str,"UTF-8")
                }
                in.close();
                String neighborData = strBuffer.toString();
                if(neighborData.length() == 13){
                    System.out.println("no active neighbor");
                    Thread.sleep(5000);
                    continue;
                }
                neighborData = neighborData.substring(11,neighborData.length() - 2);
                //System.out.println(neighborData);
                String[] pairs = neighborData.split("}");
                for(int i = 0;i<pairs.length;i++){
                    pairs[i] = pairs[i] + "}";
                    if(pairs[i].charAt(0)==',') pairs[i] = pairs[i].substring(1);
                }
                String[] nei = new String[pairs.length];
                StringBuffer neighborMap = new StringBuffer();
                neighborMap.append("\""+uuid+"\""+":{");
                for(int i = 0;i<pairs.length;i++){
                    JSONObject jsonObject = (JSONObject) JSONValue.parse(pairs[i]);
                    //System.out.println(jsonObject.get("uuid").toString());
                    neighborMap.append("\""+jsonObject.get("uuid").toString()+"\"");
                    neighborMap.append(":");
                    neighborMap.append(jsonObject.get("metric").toString());
                    neighborMap.append(",");
                    nei[i] = "\""+jsonObject.get("uuid").toString()+"\""+":"+"{"+"\""+uuid+"\""+":"+jsonObject.get("metric").toString()+"}";
                    //System.out.println(nei[i]);
                }
                String mine = neighborMap.toString();
                mine = mine.substring(0,mine.length()-1)+"}";
                String finalNeiborghMap = "{"+mine + ",";
                for(String ne:nei){
                    finalNeiborghMap = finalNeiborghMap + ne +',';
                }
                finalNeiborghMap = finalNeiborghMap.substring(0,finalNeiborghMap.length()-1)+"}";
                //System.out.println("mine: "+mine);
                try{
                    BufferedWriter bw = new BufferedWriter(new FileWriter("./json/neighborsmap.json"));
                    bw.write(finalNeiborghMap.toString());
                    bw.close();
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }
                Thread.sleep(2000);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        }
    }
    public static class LinkStateAdvertisement implements Runnable{
        private DatagramSocket udpSocket;
        private String nodeName;
        public LinkStateAdvertisement(DatagramSocket udpSocket,String nodeName) {
            this.udpSocket = udpSocket;
            this.nodeName = nodeName;
        }
        @Override
        public void run() {
            JSONParser parser = new JSONParser();
            try{
                int SeqNum = 1;
                //Thread.sleep(5000);
                while(true){
                    try {
                        Object obj = parser.parse(new FileReader("./json/neighbors.json"));
                        JSONObject jsonObject = (JSONObject) obj;
                        JSONArray neighbors = (JSONArray) jsonObject.get("active");
                        Iterator iterator = neighbors.iterator();

                        //StringBuilder adjnodes = new StringBuilder();
                        List<String> sendOut = new ArrayList<>();

                        StringBuilder sendOutMap = new StringBuilder();

                        try{
                            BufferedReader br
                                    = new BufferedReader(new FileReader("./json/map.json"));
                            sendOutMap = new StringBuilder(br.readLine());
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                        System.out.println("map:" + sendOutMap);

                        while (iterator.hasNext()) {
                            JSONObject instance = (JSONObject) iterator.next();
                            //String uuid = (String) instance.get("uuid");
                            //String name = (String) instance.get("name");
                            String host = (String) instance.get("host");
                            long port = (long) instance.get("backend");
                            //long metric = (long) instance.get("metric");

                            sendOut.add(host+","+port);
                        }

                        sendOutMap.append(";"+SeqNum+";").insert(0,";"+nodeName+";");

                        for(String send: sendOut){
                            String host = send.split(",")[0];
                            int port = Integer.parseInt(send.split(",")[1]);

                            byte[] lsaTag = "LSA".getBytes();
                            byte[] lsaData = sendOutMap.toString().getBytes();
                            byte[] lsaContent = new byte[DATA_SIZE + 3];

                            System.arraycopy(lsaTag, 0, lsaContent, 0, 3);
                            System.arraycopy(lsaData, 0, lsaContent, 3, lsaData.length);

                            DatagramPacket lsaPacket = new DatagramPacket(lsaContent, lsaContent.length, InetAddress.getByName(host), port);
                            udpSocket.send(lsaPacket);

                            System.out.println(lsaData.length);
                            System.out.println("Link state to " + host + " " + sendOutMap);
                        }
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    //send whenever neighbors.json update
                    Thread.sleep(30000);
                    //sequence number increase
                    SeqNum++;
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /// now to do
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket serverSocket = null;
        String confFilePath = "";

        //fields for reading up config file
        uuid = "";
        name = "";
        contentPath = "";
        peerNumber = 0;
        udpPort = 0;
        PortNumber = 0;

        List<String> peers = new ArrayList<>();

        //System.out.println(args[0]);
        //System.out.println(args[1]);

        if (args.length == 2 && args[0].compareTo("-c") == 0) {
            confFilePath = args[1];
        }
        else if (args.length > 2) {
            System.out.println("Invalid file numbers, please provide only one file.");
            System.exit(1);
        }
        else{
            confFilePath = "node.conf";
        }

        File confFile = new File(confFilePath);

        try{
            FileReader reader = new FileReader(confFile);
            Properties props = new Properties();
            props.load(reader);

            uuid = props.getProperty("uuid");
            name = props.getProperty("name");
            contentPath = props.getProperty("content_dir");
            PortNumber = Integer.parseInt(props.getProperty("frontend_port"));
            udpPort = Integer.parseInt(props.getProperty("backend_port"));
            peerNumber = Integer.parseInt(props.getProperty("peer_count"));
            /////////////////////////////////////////////////////////////////////////////////////
            for(int i = 0; i < peerNumber; i++){
                String peerInstance = props.getProperty("peer_"+ i);
                
                peerInstance = peerInstance.replace("uuid=", "");
                peerInstance = peerInstance.replace("host=", "");
                peerInstance = peerInstance.replace("frontend=", "");
                peerInstance = peerInstance.replace("backend=", "");
                peerInstance = peerInstance.replace("metric=", "");
                System.out.println(peerInstance);
                neighborsStr.add(peerInstance);
            }
            reader.close();

            if(uuid.length()==0){
                uuid = UUID.randomUUID().toString();
                props.setProperty("uuid",uuid);
                FileWriter writer = new FileWriter(confFile);
                props.store(writer, "Assign new UUID");
                writer.close();
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }


        try {
            serverSocket = new ServerSocket(PortNumber);
            System.out.println("http port set");
        } catch (IOException e) {
            System.err.println("Could not listen on port you want to listen.");
            System.exit(1);
        }

        System.out.println("Waiting for connection.....");

        try {
            DatagramSocket udpSocket = new DatagramSocket(udpPort);
            System.out.println("udp port set");
            Thread updateMap = new Thread(() -> {
                
                //while(true){
                    try{    
                        Thread.sleep(8000);
                        System.out.println("update map");
                        FileInputStream fis = new FileInputStream("./json/neighborsmap.json");
                        InputStreamReader inputStreamReader = new InputStreamReader(fis, "UTF-8");
                        BufferedReader in  = new BufferedReader(inputStreamReader);
                        StringBuffer strBuffer = new StringBuffer();
                        String str;
                        while ((str = in.readLine()) != null) {
                            strBuffer.append(str.trim());  
                        }
                        in.close();
                        if(strBuffer.toString().length()>0){
                            BufferedWriter bw = new BufferedWriter(new FileWriter("./json/map.json"));
                            bw.write(strBuffer.toString());
                            bw.close();
                        }
                        //Thread.sleep(20000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    // } catch (InterruptedException e) {
                    //     e.printStackTrace();
                    // }
                    }
                //}
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            });
            updateMap.start();
            Thread mapUpdater = new Thread(new neighborMapCalculate(uuid));
            mapUpdater.start();
            //Thread.sleep(10000);
            Thread keepaliver = new Thread(new KeepAliveThread(udpSocket, confFilePath));
            keepaliver.start();
            Thread.sleep(10000);
            System.out.println("neighbor information preprocessing...........");
            Thread receiver = new Thread(new ReceiverThread(udpSocket, confFilePath));
            receiver.start();


            Thread linkState = new Thread(new LinkStateAdvertisement(udpSocket, name));
            linkState.start();
            String inputLine = "initial url";
            String lastInputLine = "url";
            while (true) {
                
                Socket clientSocket = serverSocket.accept();
                OutputStream output = clientSocket.getOutputStream();
                //PrintWriter out = new PrintWriter(output);
                InputStream input = clientSocket.getInputStream();
                BufferedReader in = new BufferedReader( 
                    new InputStreamReader( clientSocket.getInputStream())); 
                inputLine = in.readLine();
                if((!inputLine.equals(lastInputLine)) || inputLine.contains("/peer/view")){ //inputLine.contains("/peer/view")
                    lastInputLine = inputLine;
                    EchoHandler handler = new EchoHandler(clientSocket,output,input, udpSocket, uuid, name, contentPath,
                            confFilePath,inputLine);
                    handler.start();
                }
            }
        } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
        }

        serverSocket.close();
    }
}