package com.foilen.smalltools.systemusage;

import com.foilen.smalltools.systemusage.results.CpuInfo;
import com.foilen.smalltools.systemusage.results.MemoryInfo;
import com.foilen.smalltools.systemusage.results.NetworkInfo;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.ResourceTools;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ProcUsageTest {

    public static void main(String[] args) {
        MemoryInfo memoryInfo = ProcUsage.getMemoryInfo("/proc/meminfo");
        System.out.println(JsonTools.prettyPrintWithoutNulls(memoryInfo));
    }

    private void assertNetworkInfo(NetworkInfo networkInfo, String interfaceName, long inBytes, long inPackets, long outBytes, long outPackets) {
        Assert.assertEquals(interfaceName, networkInfo.getInterfaceName());
        Assert.assertEquals(inBytes, networkInfo.getInBytes());
        Assert.assertEquals(inPackets, networkInfo.getInPackets());
        Assert.assertEquals(outPackets, networkInfo.getOutPackets());
        Assert.assertEquals(outPackets, networkInfo.getOutPackets());
    }

    @Test
    public void testGetMainCpuInfo() throws IOException {
        // Copy file
        File tmpFolder = Files.createTempDirectory("junit").toFile();
        String procStatPath = tmpFolder.getAbsolutePath() + File.separatorChar + "proc-stat";
        ResourceTools.copyToFile("proc-stat", this.getClass(), new File(procStatPath));

        // Execute
        CpuInfo cpuInfo = ProcUsage.getMainCpuInfo(procStatPath);

        // Assert
        Assert.assertEquals(27144761, cpuInfo.getUser());
        Assert.assertEquals(53247, cpuInfo.getNice());
        Assert.assertEquals(7825100, cpuInfo.getSystem());
        Assert.assertEquals(300401755, cpuInfo.getIdle());
        Assert.assertEquals(746107, cpuInfo.getIowait());
        Assert.assertEquals(82, cpuInfo.getIrq());
        Assert.assertEquals(45861, cpuInfo.getSoftirq());

        Assert.assertEquals(336216913, cpuInfo.calculateTotal());
        Assert.assertEquals(35815158, cpuInfo.calculateBusy());
        Assert.assertEquals(11, cpuInfo.calculateBusyPercent());
    }

    @Test
    public void testGetMainCpuInfo_Big() throws IOException {
        // Copy file
        File tmpFolder = Files.createTempDirectory("junit").toFile();
        String procStatPath = tmpFolder.getAbsolutePath() + File.separatorChar + "proc-stat_big";
        ResourceTools.copyToFile("proc-stat_big", this.getClass(), new File(procStatPath));

        // Execute
        CpuInfo cpuInfo = ProcUsage.getMainCpuInfo(procStatPath);

        // Assert
        Assert.assertEquals(27144761, cpuInfo.getUser());
        Assert.assertEquals(53247, cpuInfo.getNice());
        Assert.assertEquals(7825100, cpuInfo.getSystem());
        Assert.assertEquals(2181659790L, cpuInfo.getIdle());
        Assert.assertEquals(746107, cpuInfo.getIowait());
        Assert.assertEquals(82, cpuInfo.getIrq());
        Assert.assertEquals(45861, cpuInfo.getSoftirq());

        Assert.assertEquals(2217474948L, cpuInfo.calculateTotal());
        Assert.assertEquals(35815158, cpuInfo.calculateBusy());
        Assert.assertEquals(2, cpuInfo.calculateBusyPercent());
    }

    @Test
    public void testGetMemoryInfo() throws IOException {
        // Copy file
        File tmpFolder = Files.createTempDirectory("junit").toFile();
        String procMemPath = tmpFolder.getAbsolutePath() + File.separatorChar + "proc-meminfo";
        ResourceTools.copyToFile("proc-meminfo", this.getClass(), new File(procMemPath));

        // Execute
        MemoryInfo memoryInfo = ProcUsage.getMemoryInfo(procMemPath);

        Assert.assertEquals(377956000l, memoryInfo.getPhysicalAvailable());
        Assert.assertEquals(638984000l, memoryInfo.getPhysicalUsed());
        Assert.assertEquals(1016940000l, memoryInfo.getPhysicalTotal());
        Assert.assertEquals(4820480000l, memoryInfo.getSwapAvailable());
        Assert.assertEquals(179516000l, memoryInfo.getSwapUsed());
        Assert.assertEquals(4999996000l, memoryInfo.getSwapTotal());
    }

    @Test
    public void testGetNetworkInfos() throws IOException {
        // Copy file
        File tmpFolder = Files.createTempDirectory("junit").toFile();
        String procNetDevPath = tmpFolder.getAbsolutePath() + File.separatorChar + "proc-net-dev";
        ResourceTools.copyToFile("proc-net-dev", this.getClass(), new File(procNetDevPath));

        // Execute
        List<NetworkInfo> networkInfos = ProcUsage.getNetworkInfos(procNetDevPath);

        // Assert
        Assert.assertEquals(2, networkInfos.size());
        assertNetworkInfo(networkInfos.get(0), "eth0", 8265769858l, 35975533l, 71073501742l, 37114397l);
        assertNetworkInfo(networkInfos.get(1), "lo", 7817850194l, 14495896l, 7817850194l, 14495896l);
    }

}
