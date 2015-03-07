<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/test">
		<test>
			<xsl:for-each select="person">
				<employee>
					<xsl:value-of select="firstName"/>
					<xsl:value-of select="lastName"/>
				</employee>
			</xsl:for-each>
		</test>
	</xsl:template>
</xsl:stylesheet>
