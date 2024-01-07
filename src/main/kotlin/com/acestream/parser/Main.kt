package com.acestream.parser

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.acestream.parser.domain.ChannelInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.util.Properties
import java.util.TimeZone
import java.util.concurrent.TimeUnit


val langRegex = Regex("""\s*\[\w{2}\]""")
val hdRegex = Regex("""\[U?HD\]""")

class AceStreamPlaylistGenerator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val props = Properties()
            FileInputStream("params.properties").use {
                props.load(it)
            }
            val acestreamEngineUrl = props.getProperty("url.acestream.engine")
            val acesearchUrl = props.getProperty("url.acesearch.json")
            val epgUrl = props.getProperty("url.epg")
            val proxyHost = props.getProperty("proxy.host")
            val proxyPort = props.getProperty("proxy.port").toInt()
            val objectMapper = ObjectMapper()
                .registerModule(KotlinModule.Builder().build())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            val jsonUrl = URL(acesearchUrl)
            val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyHost, proxyPort))

//    val socksConnection = jsonUrl.openConnection(socksProxy)

            val builder = OkHttpClient.Builder().proxy(proxy).connectTimeout(30, TimeUnit.SECONDS)
            val client: OkHttpClient = builder.build()
            val request = Request.Builder().url(acesearchUrl).build()

            val asFile = File("as.json")
            if (asFile.exists())
                asFile.delete()
            val outBytes = client.newCall(request).execute().use {
                it.body!!.bytes()
            }

            asFile.outputStream().use {
                it.write(outBytes)
            }

            val skipGroups = listOf("turkey")
            val channels =
                objectMapper.readValue<Map<String, Collection<ChannelInfo>>>(asFile.inputStream())["channels"]!!

            PrintWriter(FileOutputStream(props.getProperty("playlist.name"))).use { out ->
                out.println("#EXTM3U url-tvg=$epgUrl tvg-shift=${TimeZone.getDefault().rawOffset} deinterlace=1 m3uautoload=1 cache=1000")
                for (channelInfo in channels) {
                    if (channelInfo.url == null || channelInfo.name == null) {
                        println("SKIP empty name=${channelInfo.name} cat=${channelInfo.cat}")
                        continue
                    }
                    val group = channelInfo.cat ?: "None"

                    if ((channelInfo.name.contains(langRegex) && !channelInfo.name.contains("[RU]"))
                        || group.lowercase() in skipGroups
                    ) {
                        println("===SKIP name=${channelInfo.name} cat=${channelInfo.cat}")
                        continue
                    }
                    val name = channelInfo.name
                        .replace(hdRegex, "HD")
                        .replace(langRegex, "")
                    println("${channelInfo.name} -> $name")
                    out.println("""#EXTINF:-1 group-title="$group" tvg-name="$name",$name""")
                    out.println("""#EXTGRP:$group""")
                    out.println("""$acestreamEngineUrl/ace/getstream?id=${channelInfo.url}&.mp4""")
                }
            }
        }
    }
}