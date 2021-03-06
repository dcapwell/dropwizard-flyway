package com.github.joschi.dropwizard.flyway;

import io.dropwizard.Configuration;
import io.dropwizard.db.DatabaseConfiguration;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.flywaydb.core.Flyway;

import java.util.SortedMap;
import java.util.TreeMap;

public class DbCommand<T extends Configuration> extends AbstractFlywayCommand<T> {
    private static final String COMMAND_NAME_ATTR = "subCommand";
    private final SortedMap<String, AbstractFlywayCommand<T>> subCommands = new TreeMap<>();

    public DbCommand(final DatabaseConfiguration<T> configuration, final Class<T> configurationClass) {
        super("db", "Run database migration tasks", configuration, configurationClass);

        addSubCommand(new DbMigrateCommand<>(configuration, configurationClass));
        addSubCommand(new DbCleanCommand<>(configuration, configurationClass));
        addSubCommand(new DbInitCommand<>(configuration, configurationClass));
        addSubCommand(new DbValidateCommand<>(configuration, configurationClass));
        addSubCommand(new DbInfoCommand<>(configuration, configurationClass));
        addSubCommand(new DbRepairCommand<>(configuration, configurationClass));
    }

    private void addSubCommand(final AbstractFlywayCommand<T> subCommand) {
        subCommands.put(subCommand.getName(), subCommand);
    }

    @Override
    public void configure(final Subparser subparser) {
        for (AbstractFlywayCommand<T> subCommand : subCommands.values()) {
            final Subparser cmdParser = subparser.addSubparsers()
                    .addParser(subCommand.getName())
                    .setDefault(COMMAND_NAME_ATTR, subCommand.getName())
                    .description(subCommand.getDescription());
            subCommand.configure(cmdParser);
        }
    }

    @Override
    public void run(final Namespace namespace, final Flyway flyway) throws Exception {
        subCommands.get(namespace.getString(COMMAND_NAME_ATTR)).run(namespace, flyway);
    }
}