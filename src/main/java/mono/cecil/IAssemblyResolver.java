package mono.cecil;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface IAssemblyResolver extends Iterable<AssemblyDefinition>, IDisposable {
    AssemblyDefinition resolve(AssemblyNameReference name, @Nullable ReaderParameters parameters);

    AssemblyDefinition resolve(String fullName, ReaderParameters parameters);

    boolean registerAssembly(AssemblyDefinition assembly);

    void addSearchDirectory(File directory);

    void removeSearchDirectory(File directory);
}
