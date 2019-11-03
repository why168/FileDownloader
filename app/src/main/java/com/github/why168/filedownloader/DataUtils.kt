package com.github.why168.filedownloader

import com.github.why168.multifiledownloader.DownLoadBean
import com.github.why168.multifiledownloader.utlis.FileUtilities
import java.util.ArrayList

/**
 * @author Edwin.Wu edwin.wu05@gmail.com
 * @version 2019-11-04 01:57
 * @since JDK1.8
 */
object DataUtils {

    @JvmStatic
    public fun mockData(collections: ArrayList<DownLoadBean>) {
        val bean1 = DownLoadBean()
        bean1.appName = "爱奇艺"
        bean1.appIcon =
            "http://f.hiphotos.bdimg.com/wisegame/pic/item/1fd98d1001e93901b446c6217cec54e736d1966d.jpg"
        bean1.url = "http://124.192.151.146/cdn/qiyiapp/20160912/180818/ap/qiyi.196.apk"
        bean1.id = FileUtilities.getMd5FileName(bean1.url)

        val bean2 = DownLoadBean()
        bean2.appName = "微信"
        bean2.appIcon =
            "http://f.hiphotos.bdimg.com/wisegame/pic/item/db0e7bec54e736d17a907ba993504fc2d4626994.jpg"
        bean2.url = "http://dldir1.qq.com/weixin/android/weixin6325android861.apk"
        bean2.id = FileUtilities.getMd5FileName(bean2.url)

        val bean3 = DownLoadBean()
        bean3.appName = "淘宝"
        bean3.appIcon = "http://p1.qhimg.com/dr/160_160_/t01c513232212e2d915.png"
        bean3.url =
            "http://m.shouji.360tpcdn.com/160317/0a2c6811b5fc9bada8e7e082fb5a9324/com.taobao.trip_3001049.apk"
        bean3.id = FileUtilities.getMd5FileName(bean3.url)

        val bean4 = DownLoadBean()
        bean4.appName = "酷狗音乐"
        bean4.appIcon =
            "http://c.hiphotos.bdimg.com/wisegame/pic/item/252309f7905298226013ce57dfca7bcb0a46d406.jpg"
        bean4.url =
            "http://downmobile.kugou.com/Android/KugouPlayer/8281/KugouPlayer_219_V8.2.8.apk"
        bean4.id = FileUtilities.getMd5FileName(bean4.url)

        val bean5 = DownLoadBean()
        bean5.appName = "网易云音乐"
        bean5.appIcon =
            "http://d.hiphotos.bdimg.com/wisegame/pic/item/354e9258d109b3decfae38fec4bf6c81800a4c17.jpg"
        bean5.url = "http://s1.music.126.net/download/android/CloudMusic_official_3.7.2_150253.apk"
        bean5.id = FileUtilities.getMd5FileName(bean5.url)

        val bean6 = DownLoadBean()
        bean6.appName = "百度手机卫士"
        bean6.appIcon =
            "http://a.hiphotos.bdimg.com/wisegame/pic/item/6955b319ebc4b7452322b1b9c7fc1e178b8215ee.jpg"
        bean6.url =
            "http://gdown.baidu.com/data/wisegame/6c795b7a341e0c69/baidushoujiweishi_3263.apk"
        bean6.id = FileUtilities.getMd5FileName(bean6.url)

        val bean7 = DownLoadBean()
        bean7.appName = "语玩"
        bean7.appIcon = "http://www.12nav.com/interface/res/icons/yuwan.png"
        bean7.url = "http://125.32.30.10/Yuwan-0.6.25.0-81075.apk"
        bean7.id = FileUtilities.getMd5FileName(bean7.url)

        val bean8 = DownLoadBean()
        bean8.appName = "全民K歌"
        bean8.appIcon =
            "http://e.hiphotos.bdimg.com/wisegame/pic/item/db99a9014c086e0639999b2f0a087bf40ad1cba5.jpg"
        bean8.url =
            "http://d3g.qq.com/musicapp/kge/877/karaoke_3.6.8.278_android_r31018_20160725154442_release_GW_D.apk"
        bean8.id = FileUtilities.getMd5FileName(bean8.url)


        val bean9 = DownLoadBean()
        bean9.appName = "魔秀桌面"
        bean9.appIcon =
            "http://e.hiphotos.bdimg.com/wisegame/pic/item/db99a9014c086e0639999b2f0a087bf40ad1cba5.jpg"
        bean9.url =
            "http://211.161.126.174/imtt.dd.qq.com/16891/41C80B55FE1051D8C09D2C2B3D17F9F3.apk?mkey=5874800846b6ee89&f=8f5d&c=0&fsname=com.moxiu.launcher_5.8.5_585.apk&csr=4d5s&p=.apk"
        bean9.id = FileUtilities.getMd5FileName(bean9.url)

        collections.clear()
        collections.add(bean1)
        collections.add(bean2)
        collections.add(bean3)
        collections.add(bean4)
        collections.add(bean5)
        collections.add(bean6)
        collections.add(bean7)
        collections.add(bean8)
        collections.add(bean9)
    }
}
