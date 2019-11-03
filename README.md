# FileDownloader 介绍


* 多任务断点下载
* 支持线程数控制：DownLoadConfig.getConfig().setMaxTasks(3);
* 支持service后台下载
* 两种布局：ListViewActivity + RecViewActivity
* 使用hanlder线程切换、activity注册Observer回调刷新UI


### 文件下载流程-状态
1. 默认(点击下载)
2. 连接中
3. 下载中
4. 等待中(排队状态)
5. 下载完毕
6. 下载失败
7. 暂停
8. 删除


### 效果图 
![Image of 示例](./Art/down.gif)

### 待开发任务清单及组件规划
1. 跟上潮流改写kotlin
2. 优化点
	* 线程 + service
	* 数据库
	* UI回调方式
3. 网络请求是否改成okhtpp？（待定）
	* 最开始用的是HttpURLConnection因为想着别用使用此组件方便（解决包重复、版本冲突）
4. 有什么疑问想法直接上[Issues](https://github.com/why168/FileDownloader/issues)


<br>
<br>
<br>

## MIT License

```
MIT License

Copyright (c) 2017 Edwin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
