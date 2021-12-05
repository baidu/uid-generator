CREATE TABLE `worker_id_latest_second` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
  `worker_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT 'worker id',
  `last_diff_second` bigint(20) NOT NULL DEFAULT '0' COMMENT 'Recent time difference',
  `worker_node_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'worker_node id',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT 'remark info',
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  `modified` bigint(20) NOT NULL DEFAULT '0' COMMENT 'modified time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `worker_id_unique_index` (`worker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Storage information of workerId and latest timestamp';