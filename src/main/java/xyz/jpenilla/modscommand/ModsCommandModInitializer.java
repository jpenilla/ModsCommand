/*
 * Mods Command
 * Copyright (c) 2020 Jason Penilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.jpenilla.modscommand;

import ca.stellardrift.confabricate.Confabricate;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import cloud.commandframework.permission.Permission;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.reference.ValueReference;

import static xyz.jpenilla.modscommand.Mods.mods;

public final class ModsCommandModInitializer implements ModInitializer {
  private static @MonotonicNonNull ModsCommandModInitializer instance;
  static final Logger LOGGER = LogManager.getLogger("Mods Command");
  private @MonotonicNonNull ModContainer modContainer;
  private @MonotonicNonNull Config config;

  @Override
  public void onInitialize() {
    instance = this;

    this.modContainer = FabricLoader.getInstance().getModContainer("mods-command")
      .orElseThrow(() -> new IllegalStateException("Could not find mod container for Mods Command"));
    this.loadConfig();

    final Mods mods = mods(); // Initialize so it can't fail later
    LOGGER.info("Mods Command detected {} loaded mods.", mods.allMods().count()); // We identify ourselves in log messages due to Vanilla MC's terrible Log4j config.

    final FabricServerCommandManager<Commander> manager = new FabricServerCommandManager<>(
      CommandExecutionCoordinator.simpleCoordinator(),
      Commander.ServerCommander::new,
      commander -> ((Commander.ServerCommander) commander).source()
    );
    Commands.configureCommandManager(manager);

    final ModsCommand modsCommand = new ModsCommand("mods", Permission.of("modscommand.mods"));
    modsCommand.register(manager);

    final DumpModsCommand dumpModsCommand = new DumpModsCommand("dumpmods", Permission.of("modscommand.dumpmods"));
    dumpModsCommand.register(manager);
  }

  private void loadConfig() {
    try {
      final ValueReference<Config, CommentedConfigurationNode> reference = Confabricate.configurationFor(this.modContainer, false).referenceTo(Config.class);
      this.config = reference.get();
      reference.setAndSave(this.config);
    } catch (final ConfigurateException ex) {
      throw new RuntimeException("Failed to load config", ex);
    }
  }

  public @NonNull Config config() {
    if (this.config == null) {
      throw new IllegalStateException("Mods Command config not yet loaded!");
    }
    return this.config;
  }

  public static @NonNull ModsCommandModInitializer instance() {
    if (instance == null) {
      throw new IllegalStateException("Mods Command has not yet been initialized!");
    }
    return instance;
  }
}
