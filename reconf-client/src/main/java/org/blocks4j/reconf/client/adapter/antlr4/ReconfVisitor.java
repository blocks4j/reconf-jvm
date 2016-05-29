// Generated from /home/arss/IdeaProjects/reconf-jvm/reconf-client/src/main/resources/antlr4/Reconf.g4 by ANTLR 4.5.1
package org.blocks4j.reconf.client.adapter.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ReconfParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ReconfVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ReconfParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(ReconfParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReconfParser#structure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructure(ReconfParser.StructureContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReconfParser#collection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollection(ReconfParser.CollectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReconfParser#map}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMap(ReconfParser.MapContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReconfParser#mapEntry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapEntry(ReconfParser.MapEntryContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReconfParser#primitive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitive(ReconfParser.PrimitiveContext ctx);
}