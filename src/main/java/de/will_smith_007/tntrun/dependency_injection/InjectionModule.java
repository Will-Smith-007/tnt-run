package de.will_smith_007.tntrun.dependency_injection;

import com.google.inject.AbstractModule;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

public class InjectionModule extends AbstractModule {

    private final JavaPlugin javaPlugin;

    public InjectionModule(@NonNull JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(javaPlugin);
    }
}
