package mono.cecil;

import org.jetbrains.annotations.NotNull;

public interface IAssemblyResolverProvider {
    @NotNull
    IAssemblyResolver createResolver();
}
