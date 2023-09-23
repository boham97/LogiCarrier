package com.example.robotservice.service;

import com.example.robotservice.dto.*;
import com.example.robotservice.jpa.ShelfStockRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class RobotServiceImpl implements RobotService{
    private final ShelfStockRepository stockRepository;
    private final String[][] field = new String[9][13];
    private final HashMap<String, Road> roadHash = new HashMap<>();

    @Data
    public static class Road {
        private ArrayList<long[]> schedule;
        private boolean isCorner;
        public Road(){
            this.schedule = new ArrayList<>();
        }
    }

    public static int[][] deltas  = {
            {1, 0, -1, 0},
            {0, 1, 0, -1}
    };

    @Override
    public HashMap<Long, Pick> find(Payload payload) throws NullPointerException{
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 13; j++) {
                field[i][j] = "";
            }
        }
        //field 초기값
        field[0][2] = "0";
        field[0][5] = "1";
        field[0][8] = "2";
        field[1][2] = "3";
        field[2][2] = "3";
        field[3][2] = "3";
        field[4][2] = "6";
        field[5][2] = "11";
        field[6][2] = "11";
        field[7][2] = "11";
        field[8][2] = "14";
        field[1][5] = "4";
        field[2][5] = "4";
        field[3][5] = "4";
        field[4][5] = "8";
        field[5][5] = "12";
        field[6][5] = "12";
        field[7][5] = "12";
        field[8][5] = "16";
        field[1][7] = "5";
        field[2][7] = "5";
        field[3][7] = "5";
        field[4][7] = "10";
        field[5][7] = "13";
        field[6][7] = "13";
        field[7][7] = "13";
        field[8][7] = "18";
        field[4][3] = "7";
        field[4][4] = "7";
        field[4][6] = "9";
        field[4][7] = "9";
        field[4][9] = "25";
        field[4][10] = "25";
        field[8][3] = "15";
        field[8][4] = "15";
        field[8][6] = "17";
        field[8][7] = "17";
        field[8][9] = "26";
        field[8][10] = "26";
        field[0][11] = "24";
        field[1][11] = "32";
        field[2][11] = "32";
        field[3][11] = "32";
        field[4][11] = "28";
        field[5][11] = "29";
        field[6][11] = "29";
        field[7][11] = "29";
        field[8][11] = "30";
        for(int i = 0; i< 33; i++){
            roadHash.put(String.valueOf(i), new Road());
        }
        roadHash.get("0").isCorner = true;
        roadHash.get("1").isCorner = true;
        roadHash.get("2").isCorner = true;
        roadHash.get("24").isCorner = true;
        roadHash.get("6").isCorner = true;
        roadHash.get("8").isCorner = true;
        roadHash.get("10").isCorner = true;
        roadHash.get("28").isCorner = true;
        roadHash.get("14").isCorner = true;
        roadHash.get("16").isCorner = true;
        roadHash.get("18").isCorner = true;
        roadHash.get("30").isCorner = true;

        List<CandidateDto> candidateDtoList = new ArrayList<>();
        HashMap<Long, ArrayDeque<CandidateDto>> selected = new HashMap<>();  //stockId별로 고른 stock
        HashMap<Long, ArrayList<Integer>> request = new HashMap<>();  // 몇개 필요, 몇개 골랐는지 처음 픽하기 위해
        int minCost = 100000;
        HashMap<Long, Integer> selectHashMap = new HashMap<>();//해당선반 몇번 픽됬는지
        HashMap<Long, Pick> pickHashMap = new HashMap<>(); // 해당선반에서 몇번 스톡을 몇개 꺼내야하는지

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        //stockList를 돌면서 후보군 조회
        for(ResponseItem item : payload.getResponseItemList()){
            //몇개가 필요한지 몇개 골란는지 hashmap에 추가
            request.put(item.getId(), new ArrayList<>());
            request.get(item.getId()).add(item.getQty());
            request.get(item.getId()).add(0);

            selected.put(item.getId(), new ArrayDeque<>());

            List<Object[]> res = stockRepository.findCandidate(item.getId());
            res.forEach(obj ->{
                CandidateDto candidateDto = new CandidateDto().fromObj(obj);
                candidateDtoList.add(candidateDto);
            });
        }

        //피커위치 카프카로 받아오기 candidatedto와 동일
        request.put(0L, new ArrayList<>());
        request.get(0L).add(1);
        request.get(0L).add(0);

        selected.put(0L, new ArrayDeque<>());

        CandidateDto candidateDto1 = new CandidateDto();
        candidateDto1.setX(8);
        candidateDto1.setY(2);
        candidateDto1.setStockId(0L);
        candidateDto1.setId(1000L);
        candidateDtoList.add(candidateDto1);

        CandidateDto candidateDto2 = new CandidateDto();
        candidateDto2.setX(8);
        candidateDto2.setY(8);
        candidateDto2.setStockId(0L);
        candidateDto2.setId(1001L);
        candidateDtoList.add(candidateDto2);



        int index = 0;
        //일단 넣어요 1 1 1 2 2 에서 1 1 2 뽑는다 하면  1 1 1 2 읽어야 꽉차서 안되요
        for(CandidateDto candidateDto : candidateDtoList){
            Long stockId = candidateDto.getStockId();
            if (selected.get(stockId).size() < request.get(stockId).get(0)){
                selected.get(stockId).add(candidateDto);
                //deque에 넣을떄 몇번째인지 저장
                request.get(stockId).set(1, request.get(stockId).get(1) + 1);
                if (selectHashMap.containsKey(candidateDto.getId())){
                    selectHashMap.put(candidateDto.getId(), index);
                }else{
                    selectHashMap.put(candidateDto.getId(), 1);
                }
                index ++;
            }
        }

        //가득 차면 거리 측정
        int pickerY = selected.get(0L).peek().getY();
        CalculateResultDto res = check(minCost, selected);
        int distance = res.getDistance();
        minCost = res.getMeanCost();
        if(res.isChange()){
            pickHashMap = res.getPickHash();
        }

        //deque를 채운 index보다 작으면 패스
        for(int i = 0; i < candidateDtoList.size(); i++){
            CandidateDto candidateDto = candidateDtoList.get(i);
            long stockId = candidateDto.getStockId();
            if (request.get(stockId).get(1) < i){
                //하나 뺀다, 해당 선반 몇번 골랐는지 변경
                ArrayDeque<CandidateDto> selectedItem = selected.get(stockId);
                CandidateDto polled = selectedItem.poll();
                //제거하는 선반이 여러번 선택되었다면 --, 아니면 제거
                if(selectHashMap.get(polled.getId()) == 1){
                    selectHashMap.remove(polled.getId());
                }else {
                    selectHashMap.put(polled.getId(), selectHashMap.get(polled.getId()) - 1);
                }
                //deque에 추가
                selectedItem.add(candidateDto);
                //피커가 아니면 selectHashMap 갱신, 거리 갱신
                if (stockId != 0L){
                    if (selectHashMap.containsKey(candidateDto.getId())){
                        selectHashMap.put(candidateDto.getId(), selectHashMap.get(candidateDto.getId()) + 1);
                    }else{
                        selectHashMap.put(candidateDto.getId(), 1);
                        distance += - Math.abs(pickerY - polled.getY()) + Math.abs(pickerY - candidateDto.getY());
                        //민코스트가 작다면 해당 정보 저장하기
                        minCost = Math.min(distance, minCost);
                    }
                }else{
                    pickerY = candidateDto.getX();
                    selectHashMap.put(candidateDto.getId(), 1);
                    res = check(minCost, selected);
                    distance = res.getDistance();
                    minCost = res.getMeanCost();
                    if(res.isChange()){
                        pickHashMap = res.getPickHash();
                    }
                }
            }
        }

        System.out.println("mincost: " + minCost);
        // 이동해야하는 선반 찾고 이동해야하는 위치와 dfsㄲㄲ
        int endX = 0;
        int endY = 0;
        long pickerId = 0L;
        for(Long key : pickHashMap.keySet()) {
            if (key >= 1000) {
                pickerId = key;
                break;
            }
        }

        for (CandidateDto candidateDto : candidateDtoList) {
            if (candidateDto.getId() == pickerId) {
                endX = candidateDto.getX();
                endY = candidateDto.getY();
                break;
            }
        }

        for(Long key : pickHashMap.keySet()){
            for(CandidateDto candidateDto : candidateDtoList){
                if(candidateDto.getId() == key){
                    int x = candidateDto.getX();
                    int y = candidateDto.getY();

                    send(new int[] {x, y}, new int[] {endX, endY}, 20L);
                }
            }
        }

        return pickHashMap;
    }

    public static CalculateResultDto check(int minCost, HashMap<Long, ArrayDeque<CandidateDto>> selected) throws NullPointerException{
        boolean ischange = false;
        int pickerX = selected.get(0L).peek().getX();
        int distance = 0;
        HashMap<Long, Pick> pickHashMap = new HashMap<>();

        for (Deque<CandidateDto> deque : selected.values()){
            Iterator<CandidateDto> iterator = deque.iterator();
            while (iterator.hasNext()){
                CandidateDto now = iterator.next();

                if (! pickHashMap.containsKey(now.getId())){
                    HashMap<Long, Integer> stockInfo = new HashMap<>();
                    stockInfo.put(now.getStockId(), 1);
                    Pick pick = new Pick(stockInfo);
                    pickHashMap.put(now.getId(), pick);
                    distance += Math.abs(pickerX - now.getX());
                }else {
                    Pick pick = pickHashMap.get(now.getId());
                    HashMap<Long, Integer> stockInfo = pick.getStockInfo();
                    if (stockInfo.containsKey(now.getStockId())){
                        int qty = stockInfo.get(now.getStockId());
                        stockInfo.put(now.getStockId(), qty + 1);
                    }else{
                        stockInfo.put(now.getStockId(), 1);
                    }
                }
            }
        }
        if (minCost > distance){
            minCost = distance;
            ischange = true;

        }

        return  new CalculateResultDto(distance, minCost, ischange, pickHashMap);
    }

    public void send(int[] pick, int[] end, long turn) {
        // 선반과 상호작용할 위치 찾기
        int[] pickField = new int[2];
        int[] start = new int[2];
        if (pick[1] >= 1 && roadHash.containsKey(field[pick[0]][pick[1] -1])){
            pickField = new int[]{pick[0], pick[1] - 1};
        } else if (pick[1] + 1 < field[0].length && roadHash.containsKey(field[pick[0]][pick[1] + 1])) {
            pickField = new int[]{pick[0], pick[1] + 1};
        }
        start = new int[]{0, pickField[1]};
        dfs(start, pickField, end, turn, pick);
    }
    public void dfs(int[] start, int[] pick,  int[]end, long turn, int[] shelf){
        long[][] visit = new long[9][13];
        Stack<int[]> ans = new Stack<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 13; j++) {
                visit[i][j] = Long.MAX_VALUE;
            }
        }
        //결과
        ArrayList<String> ansKeyList = new ArrayList<>();
        ArrayList<Long> ansSizeList = new ArrayList<>();
        ArrayList<Boolean> ansDirectList = new ArrayList<>();
        ArrayList<Long> ansStartList = new ArrayList<>();
        Stack<int[]> ansStack = new Stack<>();
        long fastest  = 0;

        Stack<int[]> stack = new Stack<>();
        stack.add(new int[] {start[0], start[1], 0});
        visit[start[0]][start[1]] = 1;
        while (! stack.isEmpty()){
            int[] now = stack.peek();
            int x = now[0];
            int y = now[1];
            int direction = now[2];
            long time = visit[x][y];
            //찾으면 가장 빠른 출발 시간 찾기
            if (now[0] == end[0] && now[1] == end[1] && visit[pick[0]][pick[1]] != Long.MAX_VALUE) {
                //사용경로키 사이즈 방향 진입시간 저장
                ArrayList<String> keyList = new ArrayList<>();
                ArrayList<Long> sizeList = new ArrayList<>();
                ArrayList<Boolean> directList = new ArrayList<>();
                ArrayList<Long> startList = new ArrayList<>();

                //사용 경로의 키, 사이즈, 방향성 구하기
                int i = 0;
                while (i < stack.size()) {
                    boolean isCorver = false;
                    int nx = stack.get(i)[0];
                    int ny = stack.get(i)[1];
                    int nDirection = stack.get(i)[2];
                    keyList.add(field[nx][ny]);
                    if (nDirection %2 == 0 && !roadHash.get(keyList.get(keyList.size() - 1)).isCorner) {
                        directList.add(true);
                    } else {
                        directList.add(false);
                    }
                    sizeList.add(1L);
                    long startTime = visit[nx][ny];
                    if (i == 0){
                        startTime = 1L;
                    }else{
                        startTime = startList.get(startList.size() - 1) + sizeList.get(sizeList.size() - 2);
                    }
                    while (i < stack.size() && field[nx][ny].equals(field[stack.get(i)[0]][stack.get(i)[1]])) {
                        i++;
                        isCorver = true;
                    }
                    if(isCorver) {
                        i--;
                        long endTime = visit[stack.get(i)[0]][stack.get(i)[1]];
                        if(i != 0) {
                            sizeList.set(sizeList.size() - 1,endTime - startList.get(startList.size() - 1) - sizeList.get(sizeList.size() - 2) + 1);
                        }else{
                            sizeList.set(sizeList.size() - 1,endTime);
                        }
                    }
                    if(roadHash.get(keyList.get(keyList.size() - 1)).isCorner){
                        startList.add(startTime - sizeList.get(sizeList.size() - 1) + 1);
                    }else {
                        startList.add(startTime);
                    }

                    i++;
                }

                //안되는 시간 구하기
                ArrayList<long[]> disableTime = new ArrayList<>();
                for (int j = keyList.size() - 1; j >= 0; j--) {
                    long size = sizeList.get(j);
                    String key = keyList.get(j);
                    ArrayList<long[]> schedule = roadHash.get(key).schedule;

                    //이전 값 갱신
                    for (int k = 0; k < disableTime.size(); k++) {
                        disableTime.get(k)[0] -= size;
                        disableTime.get(k)[1] -= size;
                    }
                    //불가능한 시간 추가
                    if (directList.get(j)) {//정방향이면
                        disableTime.addAll(schedule);
                    }else { //역방향일시
                        if (roadHash.get(key).isCorner){
                            for (long[] disable : schedule) {
                                disableTime.add(new long[] {disable[0] - size + 1, disable[1]});
                            }
                        }else{
                            for (long[] disable : schedule) {
                                disableTime.add(new long[] {disable[0] - size, disable[1] + size});
                            }
                        }
                    }

                }
                //가장 빠른 되는시간 계산 및 결과 저장
                disableTime.add(new long[]{0, turn});
                disableTime.sort(new Comparator<long[]>() {
                    @Override
                    public int compare(long[] o1, long[] o2) {
                        return (int) (o1[0] - o2[0]); // 두 시간의 차가 int범위 내야한다!
                    }
                });
                boolean isChange = false;
                for(long[] disable : disableTime){
                    if(fastest < disable[0]){
                        break;
                    }else if (fastest <disable[1] + 1){
                        fastest = disable[1] + 1;
                        isChange = true;
                    }
                }
                if(isChange){
                    ansKeyList = (ArrayList<String>) keyList.clone();
                    ansSizeList = (ArrayList<Long>) sizeList.clone();
                    ansDirectList = (ArrayList<Boolean>) directList.clone();
                    ansStartList = (ArrayList<Long>) startList.clone();
                    for (int[] value : stack){
                        ansStack.add(new int[]{value[0], value[1], value[2]});
                    }
                }
                stack.pop();
                continue;
            }

            boolean logic  = true;
            for (int i = 0; i < 4; i++) {
                int dx = x + deltas[0][(i + direction)%4];
                int dy = y + deltas[1][(i + direction)%4];
                long cost = i%2 + 1;
                if (dx == pick[0] && dy == pick[1]) cost += 7; //선반 꺼내오기
                if (dx == end[0] && dy == end[1] && !(direction%2 == 0)) cost ++; //마지막에 나가는 방향으로 바꾸기
                if (0 <= dx && dx < field.length && 0 <= dy && dy < field[0].length && !field[dx][dy].equals("") && visit[dx][dy] > time + cost  && time + cost <= visit[end[0]][end[1]]) {
                    stack.add(new int[]{dx, dy, (i + direction)%4});
                    visit[dx][dy] = visit[x][y] + cost;
                    logic = false;
                    break;
                }
            }
            if (logic){
                stack.pop();
            }
        }
        // 안되는 시간 갱신
        for (int j = ansKeyList.size() - 1; j >= 0; j--) {
            long size = ansSizeList.get(j);
            long time = ansStartList.get(j);
            String key = ansKeyList.get(j);
            //불가능한 시간 추가
            if (ansDirectList.get(j)) {//정방향이면
                roadHash.get(key).schedule.add(new long[]{time + fastest - 1, time + fastest - 1});
            }else { //역방향일시
                if (roadHash.get(key).isCorner){
                    roadHash.get(key).schedule.add(new long[] {time + fastest - 1, time + size - 1 + fastest - 1});
                }else{
                        roadHash.get(key).schedule.add(new long[] {time - size + fastest - 1, time + size + fastest - 1});
                }
            }
        }
        //경로 출력
        StringBuilder sb = new StringBuilder();
        int direction = ansStack.get(0)[2];
        int i = 1;
        while (i < ansStack.size()){
            int[] now = ansStack.get(i);
            if (now[2] == direction) {
                sb.append('U');
            }else if(now[2] == (direction+1)%4){
                sb.append('R');
            }else {
                sb.append('L');
            }
            if (now[0] == pick[0] && now[1] == pick[1]){
                for (int j = 0; j < 4; j++) {
                    int dx = pick[0] + deltas[0][(j + direction) % 4];
                    int dy = pick[1] + deltas[1][(j + direction) % 4];
                    if (Arrays.equals(shelf, new int[]{dx, dy})) {
                        if (j == 3) {
                            sb.append("RUTRRUR");
                        }else {
                            sb.append("LUTLLUL");
                        }
                    }
                }
            }
            i++;
        }
        //압축
        StringBuilder pressured = new StringBuilder();
        char[] unPressured= sb.toString().toCharArray();
        int j = 1;
        pressured.append(unPressured[0]);
        pressured.append("1 "); //같은 명령이 10번 되면 안된다 귀찮아요
        while (j < unPressured.length){
            if (pressured.charAt(pressured.length() - 3) != unPressured[j]){
                pressured.append(unPressured[j]);
                pressured.append("1 ");
                j++;
                continue;
            }
            int cnt = Character.getNumericValue(pressured.charAt(pressured.length() - 2));
            while(j < unPressured.length && pressured.charAt(pressured.length() - 3) == unPressured[j]){
                cnt ++;
                j ++;
            }
            pressured.deleteCharAt(pressured.length()-1);
            pressured.deleteCharAt(pressured.length()-1);
            pressured.append(cnt);
            pressured.append(" ");
        }
        System.out.println(pressured.toString());
    }
}
