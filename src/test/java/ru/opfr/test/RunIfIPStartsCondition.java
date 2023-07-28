package ru.opfr.test;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class RunIfIPStartsCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        final Method method = extensionContext.getRequiredTestMethod();
        final RunIfIPStarts annotation = method.getDeclaredAnnotation(RunIfIPStarts.class);
        if (annotation == null) {
            throw new ExtensionConfigurationException("Could not find @" + RunIfIPStarts.class + " annotation on the method " + method);
        }
        final List<String> ipList = getAllIP();
        final boolean isAddressStartsWith = ipList.stream().anyMatch(ip -> ip.startsWith(annotation.value()));
        if (isAddressStartsWith) {
            return ConditionEvaluationResult.enabled(String.format("IP starts with %s", annotation.value()));
        }
        return ConditionEvaluationResult.disabled(String.format("IP does not start with %s", annotation.value()));
    }

    private List<String> getAllIP() {
        List<String> ipList = new ArrayList<>() ;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isLoopback()  && iface.isUp()) {
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        ipList.add(addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ipList;
    }
}
