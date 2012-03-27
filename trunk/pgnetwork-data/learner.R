# TODO: Add comment
# 
# Author: johannes
###############################################################################

require(ggplot2)
require(grDevices)
library(RMySQL)         # package RMySQL laden
drv = dbDriver("MySQL") # MySQL verwenden

con <- mysqlNewConnection(drv,"pgnetwork","pgnetwork","pgnetwork")

# listet alle Tabellen auf
dbListTables(con)

# Anfrage
data <- dbGetQuery(con, "select id, cost_2, learning_2, learning_1, init_density, rational from sweep where init_coop_rate=0.7 and init_segregation=0.5;")

data$avgCoop <- rep(0.0, length(data$id))
data$sdCoopLow <- rep(0.0, length(data$id))
data$sdCoopHigh <- rep(0.0, length(data$id))
data$avgDensity <- rep(0.0, length(data$id))

data$learning_2[which(data$rational == 1)] <- data$cost_2[which(data$rational == 1)]
data$learning_2[which(data$rational == 0)] <- 1.0 - data$learning_2[which(data$rational == 0)]

data$rational[which(data$rational == 1)] <- "forward"
data$rational[which(data$rational == 0)] <- "backward"
data$rational <- factor(data$rational, levels=c("forward", "backward"))

data$learning_2 <- as.character(data$learning_2)
data$learning_2[which(data$learning_2 == "0")] <- "0.0"
data$learning_2[which(data$learning_2 == "1")] <- "1.0"
data$learning_2 <- as.factor(data$learning_2)
data <- data[which(data$learning_2 == "0.0" | data$learning_2 == 0.2 | data$learning_2 == 0.5 | data$learning_2 == 0.8 | data$learning_2 == "1.0"),]

data <- data[which(data$learning_1 == 0.1),]
data <- data[which(data$init_density < 0.7),]

for (id in data$id){
	sweep_entries <- dbGetQuery(con, paste("select * from sweep_entry where sweep_id =", id, ";"))
	coopMean <- mean(sweep_entries$coop_rate)
	data[which(data$id==id),]$avgCoop <- coopMean
	coopSd <- sd(sweep_entries$coop_rate) / 2
	data[which(data$id==id),]$sdCoopLow <- (coopMean - coopSd)
	data[which(data$id==id),]$sdCoopHigh <- (coopMean + coopSd)
	
	densMean <- mean(sweep_entries$start_density)
	data[which(data$id==id),]$avgDensity <- densMean
}

#xlabel <- as.expression(expression( paste("networking cost (", c[2], ")") ))
xlabel <- as.expression(expression(paste("Average Initial Density (", delta[0], ")")))
ylabel <- as.expression(expression(paste("Cooperation (", mu, ", ", sigma, ")")))
#chart_title <- as.expression(expression(paste(cr[0], " = 0.5, ", sg[0], " = 0.5")))

p <- ggplot(data=data, aes(x=avgDensity, y=avgCoop, ymin=sdCoopLow, ymax=sdCoopHigh, fill=learning_2, linetype=learning_2)) + #, fill=cost_2, linetype=cost_2))  
		geom_line()+ 
		geom_ribbon(alpha=0.4) +
		facet_wrap(~ rational) +
		xlab(xlabel) + 
		ylab(ylabel) + 
	#	opts(title = chart_title) +
		ylim(-0.03, 1.0)  +   
		scale_x_continuous(breaks=c(0.0,0.15,0.3)) +
		scale_fill_grey() +
		#opts(strip.text.x = theme_text(size = 8, colour = "red", angle = 90)) + 
		guides(fill=guide_legend(title=expression(paste(c[2], " / (1 - ", l[2], ")"))), linetype=guide_legend(title=expression(paste(c[2], " / (1 - ", l[2], ")"))))

#ggsave(p, file="output.pdf", width=8, height=4)

cairo_ps(file='forBackDensity.eps', width=6, height=3)
p
dev.off()

############################################################

data <- dbGetQuery(con, "select id, learning_1, init_density from sweep where rational = 0 and learning_2 = 0.8 and init_coop_rate=0.7 and init_segregation=0.5;")

data$avgCoop <- rep(0.0, length(data$id))
data$sdCoopLow <- rep(0.0, length(data$id))
data$sdCoopHigh <- rep(0.0, length(data$id))
data$avgDensity <- rep(0.0, length(data$id))

data <- data[which(data$init_density < 0.7),]
data <- data[which(is.element(data$learning_1 , c(0.1,0.3,0.5,0.7))),]

for (id in data$id){
	sweep_entries <- dbGetQuery(con, paste("select * from sweep_entry where sweep_id =", id, ";"))
	coopMean <- mean(sweep_entries$coop_rate)
	data[which(data$id==id),]$avgCoop <- coopMean
	coopSd <- sd(sweep_entries$coop_rate) / 2
	data[which(data$id==id),]$sdCoopLow <- (coopMean - coopSd)
	data[which(data$id==id),]$sdCoopHigh <- (coopMean + coopSd)
	
	densMean <- mean(sweep_entries$start_density)
	data[which(data$id==id),]$avgDensity <- densMean
}

#xlabel <- as.expression(expression( paste("networking cost (", c[2], ")") ))
xlabel <- as.expression(expression(paste("Average Initial Density (", delta[0], ")")))
ylabel <- as.expression(expression(paste("Cooperation (", mu, ", ", sigma, ")")))
#chart_title <- as.expression(expression(paste(cr[0], " = 0.5, ", sg[0], " = 0.5")))

p <- ggplot(data=data, aes(x=avgDensity, y=avgCoop, ymin=sdCoopLow, ymax=sdCoopHigh, fill=factor(learning_1), linetype=factor(learning_1))) + #, fill=cost_2, linetype=cost_2))  
		geom_line()+ 
		geom_ribbon(alpha=0.4) +
		xlab(xlabel) + 
		ylab(ylabel) + 
		#	opts(title = chart_title) +
		#ylim(-0.03, 1.0)  +   
		scale_x_continuous(breaks=c(0.0,0.15,0.3)) +
		scale_fill_grey() +
		#opts(strip.text.x = theme_text(size = 8, colour = "red", angle = 90)) + 
		guides(fill=guide_legend(title=expression(paste("learning (", l[1], ")"))), linetype=guide_legend(title=expression(paste("learning (", l[1], ")"))))

#####################################################

data <- dbGetQuery(con, "select id, learning_1, learning_2, init_density from sweep where rational = 0 and init_coop_rate=0.7 and init_segregation=0.5;")

data$avgCoop <- rep(0.0, length(data$id))
data$sdCoopLow <- rep(0.0, length(data$id))
data$sdCoopHigh <- rep(0.0, length(data$id))
data$avgDensity <- rep(0.0, length(data$id))

data <- data[which(data$init_density == 0.5),]
data <- data[which(is.element(data$learning_1 , c(0.1,0.2,0.3,0.4))),]
data <- data[which(is.element(data$learning_2 , c(0.2,0.5,0.8))),]

for (id in data$id){
	sweep_entries <- dbGetQuery(con, paste("select * from sweep_entry where sweep_id =", id, ";"))
	coopMean <- mean(sweep_entries$coop_rate)
	data[which(data$id==id),]$avgCoop <- coopMean
	coopSd <- sd(sweep_entries$coop_rate) / 2
	data[which(data$id==id),]$sdCoopLow <- (coopMean - coopSd)
	data[which(data$id==id),]$sdCoopHigh <- (coopMean + coopSd)
	
	densMean <- mean(sweep_entries$start_density)
	data[which(data$id==id),]$avgDensity <- densMean
}

xlabel <- as.expression(expression(paste("Efficiency of Network Change (", l[2], ")")))
ylabel <- as.expression(expression(paste("Cooperation (", mu, ", ", sigma, ")")))

limits <- aes(ymin=sdCoopLow, ymax=sdCoopHigh)
dodge <- position_dodge(width=0.27) 
p <- ggplot(data=data, aes(x=learning_2, y=avgCoop, fill=factor(learning_1))) + 
		geom_bar(position=dodge, stat="identity") +
		geom_errorbar(limits, position=dodge, width=0.1)  +
		xlab(xlabel) + 
		ylab(ylabel) + 
		scale_x_continuous(breaks=c(0.2,0.5,0.8)) +
		guides(fill=guide_legend(title=expression(paste("Learning (", l[1], ")")))) + 
		scale_fill_grey() 

#ggsave(p, file="output.pdf", width=8, height=4)

cairo_ps(file='learnL1.eps', width=6, height=3)
p
dev.off()

############################################################

sweep <- dbGetQuery(con, "select id, cost_2, init_coop_rate from sweep where rational = 1 and init_density=0.2 and init_segregation=0.5;")

sweep <- sweep[which(sweep$cost_2 == 0 | sweep$cost_2 == 0.2 | sweep$cost_2 == 0.5 | sweep$cost_2 == 0.8 | sweep$cost_2 == 1.0),]

data <- data.frame(coop_rate = numeric(0), cost_2 = numeric(0), init_coop_rate = numeric(0))

for (id in sweep$id){
	sweep_entries <- dbGetQuery(con, paste("select coop_rate from sweep_entry where sweep_id =", id, ";"))
	sweep_entries$cost_2 <- rep(sweep$cost_2[which(sweep$id == id)], length(sweep_entries))
	sweep_entries$init_coop_rate <- rep(sweep$init_coop_rate[which(sweep$id == id)], length(sweep_entries))
	
	data <- rbind(data, sweep_entries)
}

p <- ggplot(data=data, aes(factor(init_coop_rate), coop_rate)) + 
		geom_boxplot(aes(fill=factor(cost_2)))   

ggsave(p, file="output.pdf", width=8, height=4.5)

#######################################

data <- dbGetQuery(con, "select id, cost_2, init_coop_rate from sweep where rational = 1 and init_density=0.2 and init_segregation=0.5;")

data$avgCoop <- rep(0.0, length(data$id))
data$sdCoopLow <- rep(0.0, length(data$id))
data$sdCoopHigh <- rep(0.0, length(data$id))

data$cost_2 <- as.character(data$cost_2)
data$cost_2[which(data$cost_2 == "0")] <- "0.0"
data$cost_2[which(data$cost_2 == "1")] <- "1.0"
data$cost_2 <- as.factor(data$cost_2)
data <- data[which(data$cost_2 == "0.0" | data$cost_2 == 0.2 | data$cost_2 == 0.5 | data$cost_2 == 0.8 | data$cost_2 == "1.0"),]

for (id in data$id){
	sweep_entries <- dbGetQuery(con, paste("select * from sweep_entry where sweep_id =", id, ";"))
	coopMean <- mean(sweep_entries$coop_rate)
	data[which(data$id==id),]$avgCoop <- coopMean
	coopSd <- sd(sweep_entries$coop_rate) / 2
	data[which(data$id==id),]$sdCoopLow <- (coopMean - coopSd)
	data[which(data$id==id),]$sdCoopHigh <- (coopMean + coopSd)
}

#xlabel <- as.expression(expression( paste("networking cost (", c[2], ")") ))
xlabel <- as.expression(expression(paste("Initial Level of Cooperation (", gamma[0], ")")))
ylabel <- as.expression(expression(paste("Cooperation (", mu, ", ", sigma, ")")))
#chart_title <- as.expression(expression(paste(cr[0], " = 0.5, ", sg[0], " = 0.5")))

limits <- aes(ymin=sdCoopLow, ymax=sdCoopHigh)
dodge <- position_dodge(width=0.2) 
p <- ggplot(data=data, aes(x=init_coop_rate, y=avgCoop, fill=factor(cost_2))) + 
		geom_bar(position=dodge, stat="identity") +
		geom_errorbar(limits, position=dodge, width=0.1)  +
		xlab(xlabel) + 
		ylab(ylabel) + 
		scale_x_continuous(breaks=c(0.3,0.5,0.7)) +
		guides(fill=guide_legend(title=expression(paste("costs (", c[2], ")")))) + 
		scale_fill_grey() 

#ggsave(p, file="output.pdf", width=8, height=4.5)

cairo_ps(file='coopCoop.eps', width=6, height=3)
p
dev.off()

##########################################################

data1 <- dbGetQuery(con, "select id, cost_2, init_density, init_segregation from sweep where rational = 1 and init_coop_rate=0.5")
data2 <- dbGetQuery(con, "select id, cost_2, init_density, init_segregation from sweep where rational = 1 and init_coop_rate=0.5")

data1$start <- rep("Cooperator", length(data1$id))
data2$start <- rep("Defector", length(data2$id))

data <- rbind(data1, data2)

data$avgCoop <- rep(0.0, length(data$id))
data$avgCoopMax <- rep(0.0, length(data$id))
data$avgCoopMin <- rep(0.0, length(data$id))
data$avgDensity <- rep(0.0, length(data$id))

data$cost_2 <- as.character(data$cost_2)
data$cost_2[which(data$cost_2 == "0")] <- "0.0"
data$cost_2[which(data$cost_2 == "1")] <- "1.0"
data$cost_2 <- as.factor(data$cost_2)
data <- data[which(data$cost_2 == 0.2 | data$cost_2 == 0.5 | data$cost_2 == 0.8),]

#data <- data[which(data$init_density == 0.3 | data$init_density == 0.5 | data$init_density == 0.7),]
data <- data[which(data$init_segregation == 0.3 | data$init_segregation == 0.5 | data$init_segregation == 0.7 ),]

for (id in data$id){
	sweep_entries <- dbGetQuery(con, paste("select * from sweep_entry where sweep_id =", id, ";"))
	meanCoopCoop <- mean(sweep_entries$ch_coop_coop)
	meanDefCoop <- mean(sweep_entries$ch_def_coop)

	data[which(data$id==id & data$start=="Cooperator"),]$avgCoop <- meanCoopCoop + meanDefCoop
	data[which(data$id==id & data$start=="Cooperator"),]$avgCoopMax <- meanCoopCoop + meanDefCoop
	data[which(data$id==id & data$start=="Cooperator"),]$avgCoopMin <- meanDefCoop
	
	data[which(data$id==id & data$start=="Defector"),]$avgCoop <- meanDefCoop
	data[which(data$id==id & data$start=="Defector"),]$avgCoopMax <- meanDefCoop
	
	data[which(data$id==id),]$avgDensity <- mean(sweep_entries$start_density)
}

xlabel <- as.expression(expression(paste("Average Initial Density (", delta[0], ")")))
ylabel <- as.expression(expression(paste("Number of Cooperators (", mu, ")")))

p <- ggplot(data=data, aes(x=avgDensity, y=avgCoop, ymin=avgCoopMin, ymax=avgCoopMax, fill=factor(start))) + #, fill=cost_2, linetype=cost_2))  
		geom_line()+ 
		geom_ribbon(alpha=0.4) +
		facet_wrap(~  init_segregation * cost_2) +
		xlab(xlabel) + 
		ylab(ylabel) + 
		scale_fill_grey() +
		scale_x_continuous(breaks=c(0.0,0.25,0.5), limits=c(0, 0.6)) +
		guides(fill=guide_legend(title="Initial Strategy"))


#ggsave(p, file="output.pdf", width=8, height=5)

cairo_ps(file='densCoopSeg.eps', width=6, height=4)
p
dev.off()




