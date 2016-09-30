
    create table machine (
        id bigint not null auto_increment,
        name varchar(255) not null,
        version bigint not null,
        primary key (id)
    );

    create table machine_ips (
        machine bigint not null,
        ips varchar(255)
    );

    create table machine_statistic_network (
        id bigint not null auto_increment,
        in_bytes bigint not null,
        interface_name varchar(255),
        out_bytes bigint not null,
        primary key (id)
    );

    create table machine_statisticfs (
        id bigint not null auto_increment,
        is_root bit not null,
        path longtext,
        total_space bigint not null,
        used_space bigint not null,
        primary key (id)
    );

    create table machine_statistics (
        id bigint not null auto_increment,
        aggregations_for_day integer not null,
        aggregations_for_hour integer not null,
        cpu_total bigint not null,
        cpu_used bigint not null,
        memory_swap_total bigint not null,
        memory_swap_used bigint not null,
        memory_total bigint not null,
        memory_used bigint not null,
        timestamp datetime,
        machine_id bigint not null,
        primary key (id)
    );

    create table machine_statistics_fs (
        machine_statistics bigint not null,
        fs bigint not null,
        primary key (machine_statistics, fs)
    );

    create table machine_statistics_networks (
        machine_statistics bigint not null,
        networks bigint not null,
        primary key (machine_statistics, networks)
    );

    alter table machine 
        add constraint UK_iy15tft5g7qcqkfxicfxy3f2y  unique (name);

    alter table machine_statistics_fs 
        add constraint UK_11xmgisdqthd1wmelad3yhifc  unique (fs);

    alter table machine_statistics_networks 
        add constraint UK_9wul3wlxdhb8cxof5nffxjbih  unique (networks);

    alter table machine_ips 
        add constraint FK_tc9tuv0to5jtdiftmfed23j0f 
        foreign key (machine) 
        references machine (id);

    alter table machine_statistics 
        add constraint FK_scag82twjx85nnmxleashms9g 
        foreign key (machine_id) 
        references machine (id);

    alter table machine_statistics_fs 
        add constraint FK_11xmgisdqthd1wmelad3yhifc 
        foreign key (fs) 
        references machine_statisticfs (id);

    alter table machine_statistics_fs 
        add constraint FK_qo4fd4qvg8nm7diar2ra0dsia 
        foreign key (machine_statistics) 
        references machine_statistics (id);

    alter table machine_statistics_networks 
        add constraint FK_9wul3wlxdhb8cxof5nffxjbih 
        foreign key (networks) 
        references machine_statistic_network (id);

    alter table machine_statistics_networks 
        add constraint FK_hm4tsun0tmpkhf4gkiv62yhyd 
        foreign key (machine_statistics) 
        references machine_statistics (id);
