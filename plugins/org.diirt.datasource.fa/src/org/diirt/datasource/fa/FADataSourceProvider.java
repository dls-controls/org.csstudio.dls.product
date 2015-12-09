package org.diirt.datasource.fa;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.DataSourceProvider;

public class FADataSourceProvider extends DataSourceProvider {

    @Override
    public DataSource createInstance() {
        FADataSource src = new FADataSource();
        return src;
    }

    @Override
    public String getName() {
        return "fa";
    }

}
