
    create table Machine (
        id bigint not null auto_increment,
        name varchar(255) not null,
        version bigint not null,
        primary key (id)
    );

    create table MachineStatisticFS (
        id bigint not null auto_increment,
        isRoot bit not null,
        path longtext,
        totalSpace bigint not null,
        usedSpace bigint not null,
        primary key (id)
    );

    create table MachineStatisticNetwork (
        id bigint not null auto_increment,
        inBytes bigint not null,
        interfaceName varchar(255),
        outBytes bigint not null,
        primary key (id)
    );

    create table MachineStatistics (
        id bigint not null auto_increment,
        aggregationsForDay integer not null,
        aggregationsForHour integer not null,
        cpuTotal bigint not null,
        cpuUsed bigint not null,
        memorySwapTotal bigint not null,
        memorySwapUsed bigint not null,
        memoryTotal bigint not null,
        memoryUsed bigint not null,
        timestamp datetime,
        machine_id bigint not null,
        primary key (id)
    );

    create table MachineStatistics_MachineStatisticFS (
        MachineStatistics_id bigint not null,
        fs_id bigint not null,
        primary key (MachineStatistics_id, fs_id)
    );

    create table MachineStatistics_MachineStatisticNetwork (
        MachineStatistics_id bigint not null,
        networks_id bigint not null,
        primary key (MachineStatistics_id, networks_id)
    );

    create table Machine_ips (
        Machine_id bigint not null,
        ips varchar(255)
    );

    alter table Machine 
        add constraint UK_n6n02m3sthemy1vpndvhiqcsx unique (name);

    alter table MachineStatistics_MachineStatisticFS 
        add constraint UK_9pdvpwxb1quuqdle1qympo5t unique (fs_id);

    alter table MachineStatistics_MachineStatisticNetwork 
        add constraint UK_debineidldpdya44fhqdfl3uv unique (networks_id);

    alter table MachineStatistics 
        add constraint FK_bu23yo359n592a4mx72datqfw 
        foreign key (machine_id) 
        references Machine (id);

    alter table MachineStatistics_MachineStatisticFS 
        add constraint FK_9pdvpwxb1quuqdle1qympo5t 
        foreign key (fs_id) 
        references MachineStatisticFS (id);

    alter table MachineStatistics_MachineStatisticFS 
        add constraint FK_bo6lhq82g5xwi18cmvjs8qxxd 
        foreign key (MachineStatistics_id) 
        references MachineStatistics (id);

    alter table MachineStatistics_MachineStatisticNetwork 
        add constraint FK_debineidldpdya44fhqdfl3uv 
        foreign key (networks_id) 
        references MachineStatisticNetwork (id);

    alter table MachineStatistics_MachineStatisticNetwork 
        add constraint FK_589kikw5bnylff7uf93uouu4e 
        foreign key (MachineStatistics_id) 
        references MachineStatistics (id);

    alter table Machine_ips 
        add constraint FK_kqxoh2uyr9jt1sp9mfe0nw3yq 
        foreign key (Machine_id) 
        references Machine (id);
