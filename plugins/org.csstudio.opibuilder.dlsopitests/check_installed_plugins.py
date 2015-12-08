#!/bin/env dls-python
import os
import sys


CURRENT_DIR = os.path.dirname(os.path.realpath(__file__))
REQUIRED_PLUGIN_FILE = os.path.join(CURRENT_DIR, 'required_plugins.txt')


# Parse arguments
if not sys.argv[1:]:
    print 'Usage:', sys.argv[0], '<path_to_built_product>', '\n'
    print 'Prints any plugins that are required but not found in'
    print 'the given product.'
    sys.exit(1)
plugin_dir = os.path.join(sys.argv[1], 'plugins')

# Compare lists of plugins
required_plugins = set([line.strip() for line in open(REQUIRED_PLUGIN_FILE)])
found_plugins = set([line.split('_')[0] for line in os.listdir(plugin_dir)])
missing_plugins = required_plugins - found_plugins

# Print out any missing plugins
for plugin in missing_plugins:
    print plugin
