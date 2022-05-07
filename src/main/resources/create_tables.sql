CREATE TABLE IF NOT EXISTS `anti_vpn` (
    `ip` varchar(15) NOT NULL,
    `vpn` tinyint(4) DEFAULT 0,
    `vpn_check` tinyint(4) DEFAULT 0,
    PRIMARY KEY (`ip`)
);