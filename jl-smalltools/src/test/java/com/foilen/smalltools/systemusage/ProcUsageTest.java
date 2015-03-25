/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.systemusage;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.systemusage.results.CpuInfo;
import com.foilen.smalltools.systemusage.results.NetworkInfo;
import com.foilen.smalltools.tools.ResourceTools;
import com.google.common.io.Files;

public class ProcUsageTest {

    private void assertNetworkInfo(NetworkInfo networkInfo, String interfaceName, long inBytes, long inPackets, long outBytes, long outPackets) {
        Assert.assertEquals(interfaceName, networkInfo.getInterfaceName());
        Assert.assertEquals(inBytes, networkInfo.getInBytes());
        Assert.assertEquals(inPackets, networkInfo.getInPackets());
        Assert.assertEquals(outPackets, networkInfo.getOutPackets());
        Assert.assertEquals(outPackets, networkInfo.getOutPackets());
    }

    @Test
    public void testGetMainCpuInfo() {
        // Copy file
        File tmpFolder = Files.createTempDir();
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
    public void testGetNetworkInfos() {
        // Copy file
        File tmpFolder = Files.createTempDir();
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
