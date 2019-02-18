package mono.cecil;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class AssemblyResolvers {
    private static final AssemblyResolvers INSTANCE = new AssemblyResolvers();

    private AssemblyResolvers() {
    }

    private ServiceLoader<IAssemblyResolverProvider> serviceLoader = ServiceLoader.load(IAssemblyResolverProvider.class, AssemblyResolvers.class.getClassLoader());

    public static IAssemblyResolver createAssemblyResolver() {
        return INSTANCE.getProvider().createResolver();
    }

    private IAssemblyResolverProvider getProvider() {
        Iterator<IAssemblyResolverProvider> iterator = serviceLoader.iterator();
        if (!iterator.hasNext())
            throw new IllegalStateException("No assembly resolver provider found");
        return iterator.next();
    }
}
