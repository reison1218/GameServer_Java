package game.loader;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.util.List;

public class AgentAttachDoMain {
    public static void main(String[] args) throws Exception {
        //注意:VirtualMachineDescriptor这个类归属于lib/tools.jar,java8是没有把他自动加载进来的,需要在Platform settings里加入jdk的这个包
        List<VirtualMachineDescriptor> virtualMachineDescriptors = VirtualMachine.list();
        String name;
        for (VirtualMachineDescriptor descriptor : virtualMachineDescriptors) {
            name = descriptor.displayName();
            if (name.endsWith("http-server.jar")) {
                try {
                    VirtualMachine virtualMachine = VirtualMachine.attach(descriptor.id());
                    virtualMachine.loadAgent("E:\\server\\httpserver\\http-server.jar");//这个就是agent-demo打完包的路径
                    virtualMachine.detach();
                } catch (AttachNotSupportedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AgentLoadException e) {
                    e.printStackTrace();
                } catch (AgentInitializationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}