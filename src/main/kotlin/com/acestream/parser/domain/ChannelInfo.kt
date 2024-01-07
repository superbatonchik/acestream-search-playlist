package com.acestream.parser.domain

/**
 * #EXTM3U url-tvg=https://iptvx.one/epg/epg.xml.gz tvg-shift=4 deinterlace=1 m3uautoload=1 cache=1000
 * #EXTINF:-1 group-title="Детские" tvg-name="2x2 `[RU]`" tvg-logo="http://static.acestream.net/sites/acestream/img/ACE-logo.png",2x2 `[RU]`
 * #EXTGRP:Детские
 * http://127.0.0.1:6878/ace/getstream?id=4326cb056ec172c0d9958cd44535045723b66068&.mp4
 */

data class ChannelInfo(
    val url: String?,
    val name: String?,
    val cat: String?
)