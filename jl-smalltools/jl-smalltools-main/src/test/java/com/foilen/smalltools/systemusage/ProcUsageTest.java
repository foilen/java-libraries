package com.foilen.smalltools.systemusage;

import com.foilen.smalltools.systemusage.results.CpuInfo;
import com.foilen.smalltools.systemusage.results.MemoryInfo;
import com.foilen.smalltools.systemusage.results.NetworkInfo;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.ResourceTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(interfaceName, networkInfo.getInterfaceName());
        Assertions.assertEquals(inBytes, networkInfo.getInBytes());
        Assertions.assertEquals(inPackets, networkInfo.getInPackets());
        Assertions.assertEquals(outPackets, networkInfo.getOutPackets());
        Assertions.assertEquals(outPackets, networkInfo.getOutPackets());
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
        Assertions.assertEquals(27144761, cpuInfo.getUser());
        Assertions.assertEquals(53247, cpuInfo.getNice());
        Assertions.assertEquals(7825100, cpuInfo.getSystem());
        Assertions.assertEquals(300401755, cpuInfo.getIdle());
        Assertions.assertEquals(746107, cpuInfo.getIowait());
        Assertions.assertEquals(82, cpuInfo.getIrq());
        Assertions.assertEquals(45861, cpuInfo.getSoftirq());

        Assertions.assertEquals(336216913, cpuInfo.calculateTotal());
        Assertions.assertEquals(35815158, cpuInfo.calculateBusy());
        Assertions.assertEquals(11, cpuInfo.calculateBusyPercent());
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
        Assertions.assertEquals(27144761, cpuInfo.getUser());
        Assertions.assertEquals(53247, cpuInfo.getNice());
        Assertions.assertEquals(7825100, cpuInfo.getSystem());
        Assertions.assertEquals(2181659790L, cpuInfo.getIdle());
        Assertions.assertEquals(746107, cpuInfo.getIowait());
        Assertions.assertEquals(82, cpuInfo.getIrq());
        Assertions.assertEquals(45861, cpuInfo.getSoftirq());

        Assertions.assertEquals(2217474948L, cpuInfo.calculateTotal());
        Assertions.assertEquals(35815158, cpuInfo.calculateBusy());
        Assertions.assertEquals(2, cpuInfo.calculateBusyPercent());
    }

    @Test
    public void testGetMemoryInfo() throws IOException {
        // Copy file
        File tmpFolder = Files.createTempDirectory("junit").toFile();
        String procMemPath = tmpFolder.getAbsolutePath() + File.separatorChar + "proc-meminfo";
        ResourceTools.copyToFile("proc-meminfo", this.getClass(), new File(procMemPath));

        // Execute
        MemoryInfo memoryInfo = ProcUsage.getMemoryInfo(procMemPath);

        Assertions.assertEquals(377956000l, memoryInfo.getPhysicalAvailable());
        Assertions.assertEquals(638984000l, memoryInfo.getPhysicalUsed());
        Assertions.assertEquals(1016940000l, memoryInfo.getPhysicalTotal());
        Assertions.assertEquals(4820480000l, memoryInfo.getSwapAvailable());
        Assertions.assertEquals(179516000l, memoryInfo.getSwapUsed());
        Assertions.assertEquals(4999996000l, memoryInfo.getSwapTotal());
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
        Assertions.assertEquals(2, networkInfos.size());
        assertNetworkInfo(networkInfos.getFirst(), "eth0", 8265769858l, 35975533l, 71073501742l, 37114397l);
        assertNetworkInfo(networkInfos.get(1), "lo", 7817850194l, 14495896l, 7817850194l, 14495896l);
    }

}
