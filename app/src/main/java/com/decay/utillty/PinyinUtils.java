/*
 * Copyright 2017 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.decay.utillty;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.LruCache;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PinyinUtils {
    private static final String UNI2PY_DAT = "uni2py.dat";
    private static final int UNI2PY_DATA_SIZE = 20902;

    private static PinyinUtils INSTANCE;

    private static LruCache<String, String> fullPinyinCache = new LruCache<>(1024);
    private static LruCache<String, String> jianpinCache = new LruCache<>(1024);
    private AssetManager assetManager;

    private static final int[] INDEX_BOUNDARY = new int[] {
        0, 64, 91, 96, 123, 8543, 8576, 12295, 19967, 40870, 65280, 65375, Integer.MAX_VALUE
    };
    private static final String[] PINYIN_DICTIONARY;
    private static short[] INDEX_MAP = null;

    private static final String[] SPECIAL = new String[] {
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "50", "100", "500", "1000"
    };

    static {
        String[] pinyin = new String[405];
        pinyin[0] = "?";
        pinyin[1] = "a";
        pinyin[2] = "ai";
        pinyin[3] = "an";
        pinyin[4] = "ang";
        pinyin[5] = "ao";
        pinyin[6] = "ba";
        pinyin[7] = "bai";
        pinyin[8] = "ban";
        pinyin[9] = "bang";
        pinyin[10] = "bao";
        pinyin[11] = "bei";
        pinyin[12] = "ben";
        pinyin[13] = "beng";
        pinyin[14] = "bi";
        pinyin[15] = "bian";
        pinyin[16] = "biao";
        pinyin[17] = "bie";
        pinyin[18] = "bin";
        pinyin[19] = "bing";
        pinyin[20] = "bo";
        pinyin[21] = "bu";
        pinyin[22] = "ca";
        pinyin[23] = "cai";
        pinyin[24] = "can";
        pinyin[25] = "cang";
        pinyin[26] = "cao";
        pinyin[27] = "ce";
        pinyin[28] = "ceng";
        pinyin[29] = "cha";
        pinyin[30] = "chai";
        pinyin[31] = "chan";
        pinyin[32] = "chang";
        pinyin[33] = "chao";
        pinyin[34] = "che";
        pinyin[35] = "chen";
        pinyin[36] = "cheng";
        pinyin[37] = "chi";
        pinyin[38] = "chong";
        pinyin[39] = "chou";
        pinyin[40] = "chu";
        pinyin[41] = "chuai";
        pinyin[42] = "chuan";
        pinyin[43] = "chuang";
        pinyin[44] = "chui";
        pinyin[45] = "chun";
        pinyin[46] = "chuo";
        pinyin[47] = "ci";
        pinyin[48] = "cong";
        pinyin[49] = "cou";
        pinyin[50] = "cu";
        pinyin[51] = "cuan";
        pinyin[52] = "cui";
        pinyin[53] = "cun";
        pinyin[54] = "cuo";
        pinyin[55] = "da";
        pinyin[56] = "dai";
        pinyin[57] = "dan";
        pinyin[58] = "dang";
        pinyin[59] = "dao";
        pinyin[60] = "de";
        pinyin[61] = "deng";
        pinyin[62] = "di";
        pinyin[63] = "dian";
        pinyin[64] = "diao";
        pinyin[65] = "die";
        pinyin[66] = "ding";
        pinyin[67] = "diu";
        pinyin[68] = "dong";
        pinyin[69] = "dou";
        pinyin[70] = "du";
        pinyin[71] = "duan";
        pinyin[72] = "dui";
        pinyin[73] = "dun";
        pinyin[74] = "duo";
        pinyin[75] = "e";
        pinyin[76] = "en";
        pinyin[77] = "er";
        pinyin[78] = "fa";
        pinyin[79] = "fan";
        pinyin[80] = "fang";
        pinyin[81] = "fei";
        pinyin[82] = "fen";
        pinyin[83] = "feng";
        pinyin[84] = "fu";
        pinyin[85] = "fou";
        pinyin[86] = "ga";
        pinyin[87] = "gai";
        pinyin[88] = "gan";
        pinyin[89] = "gang";
        pinyin[90] = "gao";
        pinyin[91] = "ge";
        pinyin[92] = "ji";
        pinyin[93] = "gen";
        pinyin[94] = "geng";
        pinyin[95] = "gong";
        pinyin[96] = "gou";
        pinyin[97] = "gu";
        pinyin[98] = "gua";
        pinyin[99] = "guai";
        pinyin[100] = "guan";
        pinyin[101] = "guang";
        pinyin[102] = "gui";
        pinyin[103] = "gun";
        pinyin[104] = "guo";
        pinyin[105] = "ha";
        pinyin[106] = "hai";
        pinyin[107] = "han";
        pinyin[108] = "hang";
        pinyin[109] = "hao";
        pinyin[110] = "he";
        pinyin[111] = "hei";
        pinyin[112] = "hen";
        pinyin[113] = "heng";
        pinyin[114] = "hong";
        pinyin[115] = "hou";
        pinyin[116] = "hu";
        pinyin[117] = "hua";
        pinyin[118] = "huai";
        pinyin[119] = "huan";
        pinyin[120] = "huang";
        pinyin[121] = "hui";
        pinyin[122] = "hun";
        pinyin[123] = "huo";
        pinyin[124] = "jia";
        pinyin[125] = "jian";
        pinyin[126] = "jiang";
        pinyin[127] = "qiao";
        pinyin[128] = "jiao";
        pinyin[129] = "jie";
        pinyin[130] = "jin";
        pinyin[131] = "jing";
        pinyin[132] = "jiong";
        pinyin[133] = "jiu";
        pinyin[134] = "ju";
        pinyin[135] = "juan";
        pinyin[136] = "jue";
        pinyin[137] = "jun";
        pinyin[138] = "ka";
        pinyin[139] = "kai";
        pinyin[140] = "kan";
        pinyin[141] = "kang";
        pinyin[142] = "kao";
        pinyin[143] = "ke";
        pinyin[144] = "ken";
        pinyin[145] = "keng";
        pinyin[146] = "kong";
        pinyin[147] = "kou";
        pinyin[148] = "ku";
        pinyin[149] = "kua";
        pinyin[150] = "kuai";
        pinyin[151] = "kuan";
        pinyin[152] = "kuang";
        pinyin[153] = "kui";
        pinyin[154] = "kun";
        pinyin[155] = "kuo";
        pinyin[156] = "la";
        pinyin[157] = "lai";
        pinyin[158] = "lan";
        pinyin[159] = "lang";
        pinyin[160] = "lao";
        pinyin[161] = "le";
        pinyin[162] = "lei";
        pinyin[163] = "leng";
        pinyin[164] = "li";
        pinyin[165] = "lia";
        pinyin[166] = "lian";
        pinyin[167] = "liang";
        pinyin[168] = "liao";
        pinyin[169] = "lie";
        pinyin[170] = "lin";
        pinyin[171] = "ling";
        pinyin[172] = "liu";
        pinyin[173] = "long";
        pinyin[174] = "lou";
        pinyin[175] = "lu";
        pinyin[176] = "luan";
        pinyin[177] = "lue";
        pinyin[178] = "lun";
        pinyin[179] = "luo";
        pinyin[180] = "ma";
        pinyin[181] = "mai";
        pinyin[182] = "man";
        pinyin[183] = "mang";
        pinyin[184] = "mao";
        pinyin[185] = "me";
        pinyin[186] = "mei";
        pinyin[187] = "men";
        pinyin[188] = "meng";
        pinyin[189] = "mi";
        pinyin[190] = "mian";
        pinyin[191] = "miao";
        pinyin[192] = "mie";
        pinyin[193] = "min";
        pinyin[194] = "ming";
        pinyin[195] = "miu";
        pinyin[196] = "mo";
        pinyin[197] = "mou";
        pinyin[198] = "mu";
        pinyin[199] = "na";
        pinyin[200] = "nai";
        pinyin[201] = "nan";
        pinyin[202] = "nang";
        pinyin[203] = "nao";
        pinyin[204] = "ne";
        pinyin[205] = "nei";
        pinyin[206] = "nen";
        pinyin[207] = "neng";
        pinyin[208] = "ni";
        pinyin[209] = "nian";
        pinyin[210] = "niang";
        pinyin[211] = "niao";
        pinyin[212] = "nie";
        pinyin[213] = "nin";
        pinyin[214] = "ning";
        pinyin[215] = "niu";
        pinyin[216] = "nong";
        pinyin[217] = "nu";
        pinyin[218] = "nuan";
        pinyin[219] = "nue";
        pinyin[220] = "yao";
        pinyin[221] = "nuo";
        pinyin[222] = "o";
        pinyin[223] = "ou";
        pinyin[224] = "pa";
        pinyin[225] = "pai";
        pinyin[226] = "pan";
        pinyin[227] = "pang";
        pinyin[228] = "pao";
        pinyin[229] = "pei";
        pinyin[230] = "pen";
        pinyin[231] = "peng";
        pinyin[232] = "pi";
        pinyin[233] = "pian";
        pinyin[234] = "piao";
        pinyin[235] = "pie";
        pinyin[236] = "pin";
        pinyin[237] = "ping";
        pinyin[238] = "po";
        pinyin[239] = "pou";
        pinyin[240] = "pu";
        pinyin[241] = "qi";
        pinyin[242] = "qia";
        pinyin[243] = "qian";
        pinyin[244] = "qiang";
        pinyin[245] = "qie";
        pinyin[246] = "qin";
        pinyin[247] = "qing";
        pinyin[248] = "qiong";
        pinyin[249] = "qiu";
        pinyin[250] = "qu";
        pinyin[251] = "quan";
        pinyin[252] = "que";
        pinyin[253] = "qun";
        pinyin[254] = "ran";
        pinyin[255] = "rang";
        pinyin[256] = "rao";
        pinyin[257] = "re";
        pinyin[258] = "ren";
        pinyin[259] = "reng";
        pinyin[260] = "ri";
        pinyin[261] = "rong";
        pinyin[262] = "rou";
        pinyin[263] = "ru";
        pinyin[264] = "ruan";
        pinyin[265] = "rui";
        pinyin[266] = "run";
        pinyin[267] = "ruo";
        pinyin[268] = "sa";
        pinyin[269] = "sai";
        pinyin[270] = "san";
        pinyin[271] = "sang";
        pinyin[272] = "sao";
        pinyin[273] = "se";
        pinyin[274] = "sen";
        pinyin[275] = "seng";
        pinyin[276] = "sha";
        pinyin[277] = "shai";
        pinyin[278] = "shan";
        pinyin[279] = "shang";
        pinyin[280] = "shao";
        pinyin[281] = "she";
        pinyin[282] = "shen";
        pinyin[283] = "sheng";
        pinyin[284] = "shi";
        pinyin[285] = "shou";
        pinyin[286] = "shu";
        pinyin[287] = "shua";
        pinyin[288] = "shuai";
        pinyin[289] = "shuan";
        pinyin[290] = "shuang";
        pinyin[291] = "shui";
        pinyin[292] = "shun";
        pinyin[293] = "shuo";
        pinyin[294] = "si";
        pinyin[295] = "song";
        pinyin[296] = "sou";
        pinyin[297] = "su";
        pinyin[298] = "suan";
        pinyin[299] = "sui";
        pinyin[300] = "sun";
        pinyin[301] = "suo";
        pinyin[302] = "ta";
        pinyin[303] = "tai";
        pinyin[304] = "tan";
        pinyin[305] = "tang";
        pinyin[306] = "tao";
        pinyin[307] = "te";
        pinyin[308] = "teng";
        pinyin[309] = "ti";
        pinyin[310] = "tian";
        pinyin[311] = "tiao";
        pinyin[312] = "tie";
        pinyin[313] = "ting";
        pinyin[314] = "tong";
        pinyin[315] = "tou";
        pinyin[316] = "tu";
        pinyin[317] = "tuan";
        pinyin[318] = "tui";
        pinyin[319] = "tun";
        pinyin[320] = "tuo";
        pinyin[321] = "wa";
        pinyin[322] = "wai";
        pinyin[323] = "wan";
        pinyin[324] = "wang";
        pinyin[325] = "wei";
        pinyin[326] = "wen";
        pinyin[327] = "weng";
        pinyin[328] = "wo";
        pinyin[329] = "wu";
        pinyin[330] = "xi";
        pinyin[331] = "xia";
        pinyin[332] = "xian";
        pinyin[333] = "xiang";
        pinyin[334] = "xiao";
        pinyin[335] = "xie";
        pinyin[336] = "xin";
        pinyin[337] = "xing";
        pinyin[338] = "xiong";
        pinyin[339] = "xiu";
        pinyin[340] = "xu";
        pinyin[341] = "xuan";
        pinyin[342] = "xue";
        pinyin[343] = "xun";
        pinyin[344] = "ya";
        pinyin[345] = "yan";
        pinyin[346] = "yang";
        pinyin[347] = "ye";
        pinyin[348] = "yi";
        pinyin[349] = "yin";
        pinyin[350] = "ying";
        pinyin[351] = "yo";
        pinyin[352] = "yong";
        pinyin[353] = "you";
        pinyin[354] = "yu";
        pinyin[355] = "yuan";
        pinyin[356] = "yue";
        pinyin[357] = "yun";
        pinyin[358] = "za";
        pinyin[359] = "zai";
        pinyin[360] = "zan";
        pinyin[361] = "zang";
        pinyin[362] = "zao";
        pinyin[363] = "ze";
        pinyin[364] = "zei";
        pinyin[365] = "zen";
        pinyin[366] = "zeng";
        pinyin[367] = "zha";
        pinyin[368] = "zhai";
        pinyin[369] = "zhan";
        pinyin[370] = "zhang";
        pinyin[371] = "zhao";
        pinyin[372] = "zhe";
        pinyin[373] = "zhen";
        pinyin[374] = "zheng";
        pinyin[375] = "zhi";
        pinyin[376] = "zhong";
        pinyin[377] = "zhou";
        pinyin[378] = "zhu";
        pinyin[379] = "zhua";
        pinyin[380] = "zhuai";
        pinyin[381] = "zhuan";
        pinyin[382] = "zhuang";
        pinyin[383] = "zhui";
        pinyin[384] = "zhun";
        pinyin[385] = "zhuo";
        pinyin[386] = "zi";
        pinyin[387] = "zong";
        pinyin[388] = "zou";
        pinyin[389] = "zu";
        pinyin[390] = "zuan";
        pinyin[391] = "zui";
        pinyin[392] = "zun";
        pinyin[393] = "zuo";
        pinyin[394] = "ei";
        pinyin[395] = "m";
        pinyin[396] = "n";
        pinyin[397] = "dia";
        pinyin[398] = "cen";
        pinyin[399] = "nou";
        pinyin[400] = "jv";
        pinyin[401] = "qv";
        pinyin[402] = "xv";
        pinyin[403] = "lv";
        pinyin[404] = "nv";
        PINYIN_DICTIONARY = pinyin;
    }

    public static PinyinUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PinyinUtils(context);
        }
        return INSTANCE;
    }

    private PinyinUtils(Context context) {
        this.assetManager = context.getAssets();
    }

    private void initIndexMap() {
        synchronized (PinyinUtils.class) {
            if (INDEX_MAP != null) {
                return;
            }

            INDEX_MAP = new short[UNI2PY_DATA_SIZE];
            InputStream in = null;
            BufferedInputStream reader = null;
            try {
                final int bufferSize = 2048;
                byte[] buffer = new byte[bufferSize];
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                in = assetManager.open(UNI2PY_DAT);
                reader = new BufferedInputStream(in);
                int i = 0;
                int read = reader.read(buffer, 0, bufferSize) / 2;
                while (read != 0 && i + read <= UNI2PY_DATA_SIZE) {
                    byteBuffer.position(0);
                    byteBuffer.asShortBuffer().get(INDEX_MAP, i, Math.min(read, UNI2PY_DATA_SIZE - i));
                    i += read;
                    read = reader.read(buffer, 0, bufferSize) / 2;
                }
            } catch (Exception e) {
                // TODO: 2017/1/3
            } finally {
                Toolkit.tryToClose(reader, in);
            }
        }
    }

    public String getFullPinyin(String source) {
        return getPinyin(source, true);
    }

    public String getJianpin(String source) {
        return getPinyin(source, false);
    }

    /**
     * @param input 需要获取拼音的汉字
     * @param full 是否为完整的拼音
     */
    public String getPinyin(String input, boolean full) {
        if (input == null || input.length() == 0) {
            return "";
        }

        // get from cache
        String spelling = full ? fullPinyinCache.get(input) : jianpinCache.get(input);
        if (spelling != null) {
            return spelling;
        }

        if (INDEX_MAP == null) {
            initIndexMap();
        }

        int next = 0;
        StringBuilder builder = new StringBuilder();
        char first = input.charAt(0);
        if (first == '单') {
            builder.append(full ? "shan" : 's');
            next = 1;
        } else if (first == '仇') {
            builder.append(full ? "qiu" : 'q');
            next = 1;
        } else if (first == '曾') {
            builder.append(full ? "zeng" : 'z');
            next = 1;
        } else if (first == '万' && input.length() > 1 && input.charAt(1) == '俟') {
            builder.append(full ? "moqi" : "mq");
            next = 2;
        }

        int index = next;
        int length = input.length();
        while (index < length) {
            first = input.charAt(index);
            switch (Arrays.binarySearch(INDEX_BOUNDARY, first)) {
                case -12:
                    builder.append(Character.toLowerCase((char) (first - 65248)));
                    next = index;
                    break;
                case -10:
                    if (full) {
                        builder.append(PINYIN_DICTIONARY[INDEX_MAP[first - 19968]]);
                    } else {
                        builder.append(PINYIN_DICTIONARY[INDEX_MAP[first - 19968]].charAt(0));
                    }
                    next = index;
                    break;
                case -7:
                    next = first - 8544;
                    if (next >= SPECIAL.length) {
                        next -= SPECIAL.length;
                    }
                    builder.append(SPECIAL[next]);
                    next = index;
                    break;
                case -5:
                    builder.append(first);
                    next = index;
                    break;
                case -3:
                    builder.append(Character.toLowerCase(first));
                    next = index;
                    break;
                case 5:
                    builder.append(full ? "ling" : 'l');
                    next = index;
                    break;
                default:
                    if (!Character.isHighSurrogate(first)) {
                        builder.append(first);
                        next = index;
                    } else {
                        builder.append("??");
                        next = index + 1;
                    }
                    break;
            }
            index = next + 1;
        }

        spelling = builder.toString();
        if (full) {
            fullPinyinCache.put(input, spelling);
        } else {
            jianpinCache.put(input, spelling);
        }
        return spelling;
    }
}
